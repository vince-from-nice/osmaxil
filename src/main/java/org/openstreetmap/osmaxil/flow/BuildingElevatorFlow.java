package org.openstreetmap.osmaxil.flow;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.CloudPointImport;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("BuildingEnhancer")
@Lazy
public class BuildingElevatorFlow extends AbstractElevatorFlow<BuildingElement, CloudPointImport> {

	// =========================================================================
	// Overrided methods
	// =========================================================================

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
		data = this.dsmDataSource.findAllElevationsByGeometry(element.getGeometryString(), element.getInnerGeometryString(), srid);
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
	protected BuildingElement instanciateElement(long osmId) {
		return new BuildingElement(osmId);
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
