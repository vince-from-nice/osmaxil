package org.openstreetmap.osmaxil.flow;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.ElevationImport;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("BuildingElevator")
@Lazy
public class BuildingElevatorFlow extends AbstractElevatorFlow<BuildingElement, ElevationImport> {

	// =========================================================================
	// Overrided methods
	// =========================================================================

	protected List<BuildingElement> getTargetedElements() {
		return this.osmPostgis.findBuildingsByArea(this.includingAreaString, this.excludingAreaString,
				this.filteringAreaSrid);
	}

	@Override
	protected List<ElevationImport> findMatchingImports(BuildingElement element, int srid) {
		List<ElevationImport> result = new ArrayList<ElevationImport>();
		List<Coordinates> data = new ArrayList<>();
		// Find in PostGIS all imports matching (ie. containing) the element
		if (element.getGeometryString() == null) {
			LOGGER.warn("Unable to find matching imports because element has no geometry string");
			return result;
		}
		data = this.dsm.findAllElevationsByGeometry(element.getGeometryString(),
				element.getInnerGeometryString(), this.shrinkRadius, srid);
		// Create imports from results
		for (Coordinates coordinates : data) {
			ElevationImport imp = new ElevationImport(coordinates);
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
	public float computeElementMatchingScore(BuildingElement element, float minMatchingScore) {
		element.setMatchingScore(0);

		LOGGER.info("The number of total matching points is: " + element.getMatchingImports().size());

		// Check if the total of matching imports is fine
		if (element.getMatchingImports().size() < this.minMatchingPoints) {
			LOGGER.info("Element has only " + element.getMatchingImports().size()
					+ " matching imports, skipping it because minimum value is " + this.minMatchingPoints);
			return 0;
		}

		// Compute altitude of the center of the building with the DTM
		Coordinates center = this.osmPostgis.getPolygonCenter(
				(element.getRelationId() == null ? element.getOsmId() : -element.getRelationId()),
				this.dtm.getSrid());
		int altitude = (int) Math.round(this.dtm.findElevationByCoordinates(Double.parseDouble(center.x),
				Double.parseDouble(center.y), this.osmPostgis.getSrid()));
		LOGGER.info("Computed altitude is: " + altitude);

		// Find the max of all points elevations
		int max = 0;
		for (AbstractImport imp : element.getMatchingImports()) {
			int h = (int) Math.round(Double.parseDouble(((ElevationImport) imp).getZ()));
			if (h > max) {
				max = h;
			}
		}
		LOGGER.info("Max elevation is: " + max);

		// Check the max elevation is not under the ground
		if (max <= altitude) {
			LOGGER.warn("Max elevation is less than altitude, aborting...");
			return 0;
		}

		// Decrement elevation from the max until a descent value is found.
		// The goal is to find the *highest* elevation which has a valid score.
		int elevation = max;
		for (elevation = max; elevation > max - computingDistance && elevation > altitude; elevation--) {
			// Compute a matching score based on that elevation value
			int numberOfClosedPoints = 0;
			for (AbstractImport imp : element.getMatchingImports()) {
				int z = (int) Double.parseDouble(((ElevationImport) imp).getZ());
				if (z >= elevation - toleranceDelta) {
					numberOfClosedPoints++;
				}
			}
			// The matching score is the coverage of closest points
			element.setMatchingScore((float) numberOfClosedPoints / element.getMatchingImports().size());
			LOGGER.info("For height=" + elevation + " the number of closed points is: " + numberOfClosedPoints
					+ " and the matching score is: " + element.getMatchingScore());
			if (element.getMatchingScore() >= minMatchingScore) {
				LOGGER.info("Ok it's enough, the value " + elevation + " can be used");
				break;
			}
		}

		// Store the final height of the building which is the elevation minus the
		// altitude
		element.setComputedHeight(new Integer(elevation - altitude));
		LOGGER.info("Computed height is: " + element.getComputedHeight());

		return element.getMatchingScore();
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
