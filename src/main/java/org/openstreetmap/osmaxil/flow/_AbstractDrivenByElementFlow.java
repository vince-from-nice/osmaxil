package org.openstreetmap.osmaxil.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.dao.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.selector.MatchingScoreStatsGenerator;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class _AbstractDrivenByElementFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends __AbstractImportFlow<ELEMENT, IMPORT> {

	/**
	 * Existing elements which are inside the filtering areas.
	 */
	protected List<ELEMENT> targetedElement = new ArrayList<ELEMENT>();
    
    protected Map<Long, ELEMENT> updatableElements = new Hashtable<Long, ELEMENT>();

    protected Map<String, Integer> countersByTagName = new HashMap<String, Integer>();
    
    protected int counterForMatchedImports;
    
    protected int counterForMatchedElements;
	
	protected int counterForUpdatedElements;

	protected int limitForUpdatableElements = 0;
	
    @Autowired
    protected MatchingScoreStatsGenerator scoringStatsGenerator;
    
    // =========================================================================
    // Abstract methods
    // =========================================================================

	abstract protected List<ELEMENT> getTargetedElements();
	    
    abstract protected List<IMPORT> findMatchingImports(ELEMENT element, int srid);
    
    abstract float computeElementMatchingScore(ELEMENT element, float minMatchingScore);
    
	abstract protected String[] getUpdatableTagNames();

	abstract protected boolean updateElementTag(ELEMENT element, String tagName);

	abstract protected ELEMENT instanciateElement(long osmId);
	
	// =========================================================================
	// Public and protected methods
	// =========================================================================
	
	@Override
	public void process() {
		LOGGER.info("Looking in PostGIS for existing elements which are respecting the filtering areas");
		this.targetedElement = this.getTargetedElements();
		int i = 1;
		// For each targeted element, 
		for (ELEMENT element : this.targetedElement) {
			LOGGER.info(LOG_SEPARATOR);
			if (element.getOsmId() == null || element.getOsmId() == 0) {
				LOGGER.warn("Element is null, skipping it...");
				continue;
			}
	        // Find all matching imports
			LOGGER.info("Find matching imports for element " + element.getOsmId() + " (#" + i++ + ")");
	        List<IMPORT> matchingImports = this.findMatchingImports(element, this.osmPostgis.getSrid());
	        if (matchingImports.size() > 0) {
	        	this.counterForMatchedElements++;
	        	this.counterForMatchedImports += matchingImports.size();
	        }
	        // Bind element with its matching imports (in both way)
			element.getMatchingImports().addAll(matchingImports);
	        for (IMPORT imp : matchingImports) {
	            element.getMatchingImports().add(imp);
	            imp.setMatchingElement(element);
			}
	        // Display the list of import IDs
	        StringBuilder sb = new StringBuilder("Matching imports are : [ ");
	        for (AbstractImport imp : element.getMatchingImports()) {
	            sb.append(imp.getId() + " ");
	        }
	        LOGGER.info(sb.append("]").toString());
        	// Compute matching score of the element
            LOGGER.info("Computing matching score for element " + element.getOsmId());
            this.computeElementMatchingScore(element/*, this.computingDistance, this.toleranceDelta*/, this.minMatchingScore);
        	// Check if its matching score is fine
            if (element.getMatchingScore() < this.minMatchingScore) {
				LOGGER.info("Element has a matching score of " + element.getMatchingScore()
						+ ", skipping it because minimum value is " + this.minMatchingScore);
            	continue;
            }
            this.updatableElements.put(element.getOsmId(), element);
            // Check limit (useful for debug) 
			if (limitForUpdatableElements > 0 && this.updatableElements.size() == limitForUpdatableElements) {
				break;
			}
		}
		LOGGER.info(LOG_SEPARATOR);
	}
	
	@Override
	public void synchronize() {
		int counter = 1;
		for (ELEMENT element : this.updatableElements.values()) {
			LOGGER.info("Synchronizing element " + element.getOsmId() + " (#" + counter++ + ")");
            // Update its data from OSM API
        	LOGGER.info("Update data of element " + element.getOsmId() + " from OSM API");
            OsmXmlRoot apiData = this.osmStandardApi.readElement(element.getOsmId(), element.getType());
            if (apiData == null) {
                LOGGER.error("Unable to fetch data from OSM API for element#" + element.getOsmId());
                continue;
            }
            element.setApiData(apiData);        
	        // Store original values for each updatable tag
	        for (String tagName : this.getUpdatableTagNames()) {
	            element.getOriginalValuesByTagNames().put(tagName, element.getTagValue(tagName));
	        }
			boolean needToSync = false;
			// Fore each updatable tags (in theory)
			for (String updatableTagName : this.getUpdatableTagNames()) {
				// Check if it's really updatable (data should have been updated from live)
				if (this.isElementTagUpdatable(element, updatableTagName)) {
					boolean updated = this.updateElementTag(element, updatableTagName);
					if (updated) {
						needToSync = true;
						Integer counterByTag = this.countersByTagName.get(updatableTagName);
						counterByTag++;
						this.countersByTagName.put(updatableTagName, counterByTag);
					}
				} else {
					LOGGER.warn("Element tag cannot be updated");
				}
			}
			try {
				// Do the synchronization only if needed
				if (needToSync) {
					boolean success = false;
					if ("api".equals(this.synchronizationMode)) {
						success = this.osmStandardApi.writeElement(element);
					} else if ("gen".equals(this.synchronizationMode)) {
						success = this.osmXmlFile.writeToFile("" + element.getOsmId(), element.getApiData());
					}
					if (success) {
						this.counterForUpdatedElements++;
						element.setAltered(true);
						LOGGER.debug("Ok element has been synchronized");
					}
				} else {
					LOGGER.info("Element doesn't need to be synchronized");
				}
			} catch (java.lang.Exception e) {
				LOGGER.error("Synchronization of element " + element.getOsmId() + " has failed: ", e);
			}
			LOGGER.info(LOG_SEPARATOR);
		}
	}

	@Override
	public void displayLoadingStatistics() {
		LOGGER_FOR_STATS.info("=== Loading statistics ===");
		LOGGER_FOR_STATS.info("Total of targeted elements (ie. which are inside filtering areas): " + this.targetedElement.size());
	}
	
	@Override
	public void displaySynchronizingStatistics() {
		LOGGER_FOR_STATS.info("=== Synchronizing statistics ===");
		LOGGER_FOR_STATS.info("Total of updated elements: " + this.counterForUpdatedElements);
		LOGGER_FOR_STATS.info("Total of updates by tag:");
		for (String updatableTagName : this.getUpdatableTagNames()) {
			LOGGER_FOR_STATS.info("\t - total of updates on the tag [" + updatableTagName + "]: "
					+ this.countersByTagName.get(updatableTagName));
		}
	}
	
    @Override
    public void displayProcessingStatistics() {
        LOGGER_FOR_STATS.info("=== Processing statistics ===");
        LOGGER_FOR_STATS.info("Total of matched elements (ie. which have at least one matching imports): " + this.counterForMatchedElements);
        LOGGER_FOR_STATS.info("Total of matching imports: " + this.counterForMatchedImports);
		LOGGER_FOR_STATS.info("Average of matching imports for each element: "
				+ (this.counterForMatchedElements > 0 ? this.counterForMatchedImports / counterForMatchedElements : "0"));
        this.scoringStatsGenerator.displayStatsByMatchingScore((Collection<AbstractElement>) this.updatableElements.values());
        LOGGER_FOR_STATS.info("Minimum matching score is: " + this.minMatchingScore);
        LOGGER_FOR_STATS.info("Total of updatable elements: " + this.updatableElements.size());
    }
    
	// =========================================================================
	// Private methods
	// =========================================================================

	@PostConstruct
	private void init() {
		LOGGER.info("Init of " + this.getClass().getName());
		for (String updatableTagName : this.getUpdatableTagNames()) {
			this.countersByTagName.put(updatableTagName, 0);
		}
	}
	
	private boolean isElementTagUpdatable(ELEMENT element, String tagName) {
		String originalValue = element.getOriginalValuesByTagNames().get(tagName);
		if (originalValue != null) {
			LOGGER.info("The tag " + tagName + " cannot be updated because it has an original value: " + originalValue);
			return false;
		}
		return true;
	}

}
