package org.openstreetmap.osmaxil.flow;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.dao.ElevationDatabase;
import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.CloudPointImport;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.openstreetmap.osmaxil.plugin.scorer.BuildingPointCloudScorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("BuildingEnhancer")
@Lazy
public class BuildingElevatorFlow extends _AbstractDrivenByElementFlow<BuildingElement, CloudPointImport> {

	private static final String UPDATABLE_TAG_NAMES[] = new String[] { ElementTag.HEIGHT };

	@Autowired
	protected ElevationDatabase elevationDatabase;

	// =========================================================================
	// Overrided methods
	// =========================================================================

	protected void loadData() {
		this.elevationDatabase.createPointCloudTableFromXYZFiles();
	}

	protected List<BuildingElement> getTargetedElements() {
		return this.osmPostgis.findBuildingsByArea(this.includingAreaString, this.excludingAreaString,
				this.filteringAreaSrid);
	}

	@Override
	protected List<CloudPointImport> findMatchingImports(BuildingElement element, int srid) {
		List<CloudPointImport> result = new ArrayList<CloudPointImport>();
		List<Coordinates> data = new ArrayList<>();
		// Find in PostGIS all imports matching (ie. containing) the element
		if (element.getGeometryString() == null) {
			LOGGER.warn("Unable to find matching imports because element has no geometry string");
			return result;
		}
		data = this.elevationDatabase.findPointByGeometry(element.getGeometryString(), element.getInnerGeometryString(), srid);
		// Create imports from results
		for (Coordinates coordinates : data) {
			CloudPointImport imp = new CloudPointImport(coordinates);
			// Use the rounded value of 'z' as ID
			imp.setId(Long.parseLong(coordinates.z.substring(0, coordinates.z.indexOf("."))));
			// TODO need to transform lat/lon to the correct SRID ?
			imp.setLatitude(Double.parseDouble(coordinates.x));
			imp.setLongitude(Double.parseDouble(coordinates.y));
			result.add(imp);
		}
		return result;
	}

	@Override
	protected boolean isElementTagUpdatable(BuildingElement element, String tagName) {
		// Building tags are updatable only if it doesn't have an original value
		String originalValue = element.getOriginalValuesByTagNames().get(tagName);
		if (originalValue != null) {
			LOGGER.info("The tag " + tagName + " cannot be updated because it has an original value: " + originalValue);
			return false;
		}
		return true;
	}

	@Override
	protected boolean updateElementTag(BuildingElement element, String tagName) {
		boolean updated = false;
		if (ElementTag.HEIGHT.equals(tagName)) {
			if (element.getComputedHeight() == null) {
				LOGGER.error("Cannot update tag because computed height is null");
				return false;
			}
			element.setHeight(element.getComputedHeight());
			LOGGER.info("===> Updating height to [" + element.getHeight() + "]");
			updated = true;
		}
		return updated;
	}

	@Override
	public void displayProcessingStatistics() {
		super.displayProcessingStatistics();
		LOGGER_FOR_STATS.info("Specific settings of the plugin:");
		//LOGGER_FOR_STATS.info(" - Shrink radius is: " + this.shrinkRadius);
		if (this.scorer instanceof BuildingPointCloudScorer) {
			BuildingPointCloudScorer bdcs = (BuildingPointCloudScorer) this.scorer;
			LOGGER_FOR_STATS.info(" - Minimum matching point is: " + bdcs.minMatchingPoints);
			LOGGER_FOR_STATS.info(" - Tolerance delta is: " + bdcs.toleranceDelta);
		}
	}

	@Override
	protected BuildingElement instanciateElement(long osmId) {
		return new BuildingElement(osmId);
	}

	@Override
	protected String[] getUpdatableTagNames() {
		return UPDATABLE_TAG_NAMES;
	}

	// =========================================================================
	// Private methods
	// =========================================================================

	/**
	 * Returns the geometry as WKT of the inner member of a multipolygon building.
	 */
	private String getInnerGeometryString(long relationId, String membersString) {
		long innerMemberId = BuildingElement.getOuterOrInnerMemberIds(relationId, membersString, false).get(0);
		String query = "SELECT osm_id, ST_AsText(way) AS geomAsWKT, 1 FROM planet_osm_polygon WHERE building <> '' AND osm_id="
				+ innerMemberId;
		LOGGER.debug("Used query is: " + query);
		// Damned the inner member id is a key of the planet_osm_ways table which
		// doesn't store the coordinates but only point IDs :(
		// OsmPostgisDB.IdWithGeom[] innerMemberIdWithGeom =
		// this.osmPostgis.findElementIdsWithGeomByQuery(query);
		// Need to do another request to fetch points coordinates...
		return null;
	}

}