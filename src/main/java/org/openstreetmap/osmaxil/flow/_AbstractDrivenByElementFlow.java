package org.openstreetmap.osmaxil.flow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.openstreetmap.osmaxil.dao.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.scorer.AbstractElementScorer;
import org.openstreetmap.osmaxil.plugin.selector.MatchingScoreStatsGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class _AbstractDrivenByElementFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends _AbstractImportFlow<ELEMENT, IMPORT> {

	/**
	 * Existing elements which are inside the filtering areas.
	 */
	protected List<ELEMENT> targetedElement;
	
    protected Map<Long, ELEMENT> matchedElements = new Hashtable<Long, ELEMENT>();

    protected int counterForMatchedImports;
	
	protected int counterForUpdatableElements = 0;
	
	protected int counterForUpdatedElements;

	protected Map<String, Integer> countersByTagName = new HashMap<String, Integer>();
	
	protected int limitForMatchedElements = 0;
	
	@Value("${osmaxil.skipLoading}")
	protected Boolean skipLoading;
	
	@Autowired
	@Resource(name="${scorer}")
	protected AbstractElementScorer<ELEMENT> scorer;
	
    @Autowired
    protected MatchingScoreStatsGenerator scoringStatsGenerator;
	
    // =========================================================================
    // Abstract methods
    // =========================================================================

    abstract protected void loadData();
    
	abstract protected List<ELEMENT> getTargetedElements();
	    
    abstract protected List<IMPORT> findMatchingImports(ELEMENT element, int srid);
    
	abstract protected String[] getUpdatableTagNames();

	abstract protected boolean isElementTagUpdatable(ELEMENT element, String tagName);

	abstract protected boolean updateElementTag(ELEMENT element, String tagName);

	abstract protected ELEMENT instanciateElement(long osmId);
	
	// =========================================================================
	// Public and protected methods
	// =========================================================================

	@Override
	public void load() {
		if (skipLoading) {
			LOGGER.info("Skip the loading phase");
			return;
		}
		this.loadData();
	}
	
	@Override
	public void process() {
		// Fetch all targeted elements
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
			LOGGER.info("Find matching imports for element #" + element.getOsmId() + " <" + i++ + ">");
	        List<IMPORT> matchingImports = this.findMatchingImports(element, this.osmPostgis.getSrid());
	        this.counterForMatchedImports += matchingImports.size();
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
			// Check if the total of matching imports is fine
	        // TODO fix confusion between minMatchingScore and minMatchingPoints !!
			if (element.getMatchingImports().size() < this.minMatchingScore) {
				LOGGER.info("Element has only " + element.getMatchingImports().size()
						+ " matching imports, skipping it because minimum value is "
						+ this.minMatchingScore);
				continue;
			} else {
	        	this.matchedElements.put(element.getOsmId(), element);
	        }
        	// Compute matching score of the element
            LOGGER.info("Computing matching score for element #" + element.getOsmId());
            this.scorer.computeElementMatchingScore(element/*, this.computingDistance, this.toleranceDelta*/, this.minMatchingScore);
        	// Check if its matching score is fine
            if (element.getMatchingScore() < this.minMatchingScore) {
				LOGGER.info("Element has a matching score of " + element.getMatchingScore()
						+ ", skipping it because minimum value is " + this.minMatchingScore);
            	continue;
            } else {
            	this.counterForUpdatableElements++;
            }
            // Update its data from OSM API
        	LOGGER.info("Update data of element #" + element.getOsmId() + " from OSM API");
            OsmXmlRoot apiData = this.osmStandardApi.readElement(element.getOsmId(), element.getType());
            if (apiData == null) {
                LOGGER.error("Unable to fetch data from OSM API for element#" + element.getOsmId());
            } else {
    	        element.setApiData(apiData);        
    	        // Store original values for each updatable tag
    	        for (String tagName : this.getUpdatableTagNames()) {
    	            element.getOriginalValuesByTagNames().put(tagName, element.getTagValue(tagName));
    	        }
            }
            // Check limit (useful for debug) 
			if (limitForMatchedElements > 0 && this.matchedElements.size() == limitForMatchedElements) {
				break;
			}
		}
		LOGGER.info(LOG_SEPARATOR);
	}
	
	@Override
	public void synchronize() {
		int counter = 1;
		for (ELEMENT element : this.matchedElements.values()) {
			LOGGER.info("Synchronizing element #" + element.getOsmId() + " <" + counter++ + ">");
			// Check if its best matching score is enough
			if (element.getMatchingScore() < this.minMatchingScore) {
				LOGGER.info("Element cannot be synchronized because its matching score is " + element.getMatchingScore()
						+ " (min=" + this.minMatchingScore + ")");
				LOGGER.info(LOG_SEPARATOR);
				continue;
			}
			boolean needToSync = false;
			// Fore each updatable tags (in theory)
			for (String updatableTagName : this.getUpdatableTagNames()) {
				// Check if it's really updatable (data should have been update from live)
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
		LOGGER_FOR_STATS.info("Sorry but there are not loading statistics for that plugin...");
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
        LOGGER_FOR_STATS.info("Total of targeted elements (ie. which are inside filtering areas): " + this.targetedElement.size());
        LOGGER_FOR_STATS.info("Total of matched elements (ie. which have at least one matching imports): " + this.matchedElements.size());
        LOGGER_FOR_STATS.info("Total of matching imports: " + this.counterForMatchedImports);
		LOGGER_FOR_STATS.info("Average of matching imports for each element: "
				+ (this.matchedElements.size() > 0 ? this.counterForMatchedImports / this.matchedElements.size() : "0"));
        this.scoringStatsGenerator.displayStatsByMatchingScore((Collection<AbstractElement>) matchedElements.values());
        LOGGER_FOR_STATS.info("Minimum matching score is: " + this.minMatchingScore);
        LOGGER_FOR_STATS.info("Total of updatable elements: " + this.counterForUpdatableElements);
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

}
