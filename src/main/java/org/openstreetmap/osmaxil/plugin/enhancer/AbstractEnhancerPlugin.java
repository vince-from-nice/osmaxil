package org.openstreetmap.osmaxil.plugin.enhancer;

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
	
	protected int counterForMatchedElements;
	
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
			LOGGER.info("Binding element #" + element.getOsmId() + ": ");
			if (element.getOsmId() == null || element.getOsmId() == 0) {
				LOGGER.warn("Element is null, skipping it...");
				break;
			}
			this.associateImportsWithElements(element);
			LOGGER.info(LOG_SEPARATOR);
			if (this.counterForMatchedElements == limitForMatchedElements) {
				break;
			}
		}
		
        // For each matched element, compute its matching score
        int elementNbr = 0;
        for (ELEMENT element : this.matchedElements.values()) {
            LOGGER.info("Computing matching score for element #" + elementNbr + ": " + element);
            if (element == null) {
                LOGGER.warn("Element is null, skipping it...");
                break;
            }
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
		// TODO Auto-generated method stub
	}

	@Override
	public void displaySynchronizingStatistics() {
		// TODO Auto-generated method stub
	}

	// =========================================================================
	// Private methods
	// =========================================================================
	
	private void associateImportsWithElements(ELEMENT element) {
        // Update element with data from OSM API
        LOGGER.info("Updating element #" + element.getOsmId() + " from OSM API");
        OsmXmlRoot apiData = this.osmStandardApi.readElement(element.getOsmId(), ElementType.Way);
        if (apiData == null) {
            LOGGER.warn("Unable to fetch data from OSM API for element#" + element.getOsmId());
        }
        element.setApiData(apiData);        
        // Store original values for each updatable tag
        for (String tagName : this.getUpdatableTagNames()) {
            element.getOriginalValuesByTagNames().put(tagName, element.getTagValue(tagName));
        }
        // Find matching imports
        List<IMPORT> matchingImports = this.findMatchingImports(element, this.osmPostgis.getSrid());
        if (matchingImports.size() > 0) {
        	this.matchedElements.put(element.getOsmId(), element);
            this.counterForMatchedElements++;
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
	
    private ELEMENT getOrCreateElement(Long osmId) throws Exception {
        ELEMENT element = this.matchedElements.get(osmId);
        if (element == null) {
            // Fetch data from OSM API
            OsmXmlRoot apiData = this.osmStandardApi.readElement(osmId, ElementType.Way);
            if (apiData == null) {
                throw new Exception("Unable to fetch data from OSM API for element#" + osmId);
            }
            // Instanciate a new element
            element = this.instanciateElement(osmId);
            element.setApiData(apiData);
            this.matchedElements.put(osmId, element);
            // Store original values for each updatable tag
            for (String tagName : this.getUpdatableTagNames()) {
                element.getOriginalValuesByTagNames().put(tagName, element.getTagValue(tagName));
            }
        }
        return element;
    }
}
