package org.openstreetmap.osmaxil.plugin.enhancer;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractElementMatcher;

public abstract class AbstractEnhancerPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends AbstractPlugin<ELEMENT, IMPORT> {

	protected Long[] elementIds;

	abstract protected String getExistingElementQuery();

	abstract protected AbstractElementMatcher<ELEMENT> getElementMatcher();

	// =========================================================================
	// Public methods
	// =========================================================================

	@Override
	public void process() {
		// Load IDs of all existing element which are inside the filtering areas
		LOGGER.info("Looking in PostGIS for existing elements which are respecting the filtering areas");
		this.elementIds = this.getExistingElements();

		// For each existing element, associate matched imports
		for (Long elementId : this.elementIds) {
			LOGGER.info("Binding element #" + elementId + ": ");
			if (elementId == null || elementId == 0) {
				LOGGER.warn("Element is null, skipping it...");
				break;
			}
			this.associateElementWithImports(elementId);
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

	private void associateElementWithImports(Long elementId) {

	}

	private Long[] getExistingElements() {
		Long[] results;
		String condition = "ST_Intersects(way, ST_Transform(ST_GeomFromText('" + includingAreaString + "', 4326), "
				+ osmPostgis.getSrid() + "))";
		condition += " AND ST_Disjoint(way, ST_Transform(ST_GeomFromText('" + excludingAreaString + "', 4326), "
				+ osmPostgis.getSrid() + "))";
		String query = this.getExistingElementQuery();
		if (query.toUpperCase().indexOf(" WHERE ") == -1) {
			query += " WHERE " + condition;
		} else {
			query += " AND " + condition;
		}
		LOGGER.debug("Used query is: " + query);
		results = this.osmPostgis.findElementIdsByQuery(query);
		LOGGER.debug("Number of returned element IDs: " + results.length);
		return results;
	}
}
