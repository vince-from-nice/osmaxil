package org.openstreetmap.osmaxil.flow;

import java.util.Collection;
import java.util.List;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;

public abstract class AbstractEnhancerFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends AbstractUpdaterFlow<ELEMENT, IMPORT> {

	/**
	 * Existing elements which are inside the filtering areas.
	 */
	protected List<ELEMENT> targetedElement;
	
	protected int counterForUpdatableElements = 0;
	
	protected int limitForMatchedElements = 0;
	
    // =========================================================================
    // Abstract methods
    // =========================================================================

	abstract protected List<ELEMENT> getTargetedElements();
	    
    abstract protected List<IMPORT> findMatchingImports(ELEMENT element, int srid);
	
	// =========================================================================
	// Public methods
	// =========================================================================

	@Override
	public void process() {
		// Load IDs of all targeted elements
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
            this.computeMatchingScores(element);
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

}
