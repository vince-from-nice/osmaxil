package org.openstreetmap.osmaxil.plugin.enhancer;

import java.util.Collection;
import java.util.List;

import org.openstreetmap.osmaxil.Exception;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementType;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.plugin.updater.AbstractUpdaterPlugin;

public abstract class AbstractEnhancerPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends AbstractUpdaterPlugin<ELEMENT, IMPORT> {

	/**
	 * Existing elements which are inside the filtering areas.
	 */
	protected List<ELEMENT> targetedElement;
	
	protected int limitForMatchedElements = 10;

	abstract protected List<IMPORT> findMatchingImports(ELEMENT element, int srid);

	abstract protected List<ELEMENT> getTargetedElements();
	
	// =========================================================================
	// Public methods
	// =========================================================================

	@Override
	public void process() {
		// Load IDs of all targeted elements
		LOGGER.info("Looking in PostGIS for existing elements which are respecting the filtering areas");
		this.targetedElement = this.getTargetedElements();

		// For each targeted element, associate matched imports
		for (ELEMENT element : this.targetedElement) {
			LOGGER.info("Binding imports with element #" + element.getOsmId() + ": ");
			if (element.getOsmId() == null || element.getOsmId() == 0) {
				LOGGER.warn("Element is null, skipping it...");
				break;
			}
			this.associateImportsWithElements(element);
			LOGGER.info(LOG_SEPARATOR);
			if (this.matchedElements.size() == limitForMatchedElements) {
				break;
			}
		}
		
        // For each matched element 
        for (ELEMENT element : this.matchedElements.values()) {
        	LOGGER.info("Update data of element #" + element.getOsmId() + " from OSM API");
        	// Update element with data from OSM API
        	this.updateElementFromAPI(element);
        	// Compute its matching score
            LOGGER.info("Computing matching score for element #" + element.getOsmId());
            this.computeMatchingScores(element);
            LOGGER.info(LOG_SEPARATOR);
        }
	}

	@Override
	public void synchronize() {
		// TODO Auto-generated method stub
	}

    @Override
    public void displayProcessingStatistics() {
        LOGGER_FOR_STATS.info("=== Processing statistics ===");
        LOGGER_FOR_STATS.info("Total of elements which have targeted (inside the filtering area): " + this.targetedElement.size());
        LOGGER_FOR_STATS.info("Total of elements which have one or more matching imports: " + this.matchedElements.size());
        this.scoringStatsGenerator.displayStatsByMatchingScore((Collection<AbstractElement>) matchedElements.values());
    }

	@Override
	public void displaySynchronizingStatistics() {
		// TODO Auto-generated method stub
	}

	// =========================================================================
	// Private methods
	// =========================================================================
	
	private void associateImportsWithElements(ELEMENT element) {
        // Find matching imports
        LOGGER.info("Find matching imports");
        List<IMPORT> matchingImports = this.findMatchingImports(element, this.osmPostgis.getSrid());
        if (matchingImports.size() > 0) {
        	this.matchedElements.put(element.getOsmId(), element);
        }
        // Bind all the imports to the targeted element
        for (IMPORT imp : matchingImports) {
            element.getMatchingImports().add(imp);
            imp.setMatchingElement(element);
		}
        StringBuilder sb = new StringBuilder("Matching imports are : [ ");
        for (AbstractImport i : element.getMatchingImports()) {
            sb.append(i.getId() + " ");
        }
        LOGGER.info(sb.append("]").toString());
	}
	
	private void updateElementFromAPI(ELEMENT element) {
        OsmXmlRoot apiData = this.osmStandardApi.readElement(element.getOsmId(), ElementType.Way);
        if (apiData == null) {
            LOGGER.warn("Unable to fetch data from OSM API for element#" + element.getOsmId());
        } else {
	        element.setApiData(apiData);        
	        // Store original values for each updatable tag
	        for (String tagName : this.getUpdatableTagNames()) {
	            element.getOriginalValuesByTagNames().put(tagName, element.getTagValue(tagName));
	        }
        }
	}
}
