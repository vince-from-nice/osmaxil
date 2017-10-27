package org.openstreetmap.osmaxil.flow;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.dao.ElevationDataSource;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.ElevationImport;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("BuildingElevator")
@Lazy
public class BuildingElevatorFlow extends AbstractElevatorFlow<BuildingElement, ElevationImport> {

	protected List<BuildingElement> getTargetedElements() {
		return this.osmPostgis.findBuildingsByArea(this.includingAreaString, this.excludingAreaString,
				this.filteringAreaSrid);
	}

	@Override
	protected List<ElevationImport> findMatchingImports(BuildingElement element, int srid) {
		List<ElevationImport> results = new ArrayList<ElevationImport>();
		// Find in PostGIS all imports matching (ie. containing) the element
		if (element.getGeometryString() == null) {
			LOGGER.warn("Unable to find matching imports because element has no geometry string");
			return results;
		}
		results = this.dsm.findAllElevationsByGeometry(element.getGeometryString(), element.getInnerGeometryString(),
				this.dsmValueScale, this.shrinkRadius, srid);
		return results;
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
				(element.getRelationId() == null ? element.getOsmId() : -element.getRelationId()), this.dtm.getSrid());
		ElevationImport alt = this.dtm.findElevationByCoordinates(Double.parseDouble(center.x),
				Double.parseDouble(center.y), this.dtmValueScale, this.osmPostgis.getSrid());
		int altitude = (int) Math.round(alt.z);
		LOGGER.info("Computed terrain elevation is: " + altitude);

		// Check the DTM value is not outside the range of allowed values
		if (!this.checkElevationValue(altitude, ElevationDataSource.Use.DTM)) {
			LOGGER.warn("Skipping element because terrain elevation is outside the range of allowed values");
			return 0;
		}

		// Find the max of all points elevations
		int max = 0;
		for (AbstractImport imp : element.getMatchingImports()) {
			int h = (int) Math.round(((ElevationImport) imp).z);
			if (h > max) {
				max = h;
			}
		}
		LOGGER.info("Max surface elevation is: " + max);

		// Check the max elevation is not under the ground
		if (max <= altitude) {
			LOGGER.warn("Max surface elevation is less than altitude, aborting...");
			return 0;
		}

		// Decrement elevation from the max until a descent value is found.
		// The goal is to find the *highest* elevation which has a valid score.
		int elevation = max;
		for (elevation = max; elevation > max - computingDistance && elevation > altitude; elevation--) {
			// Compute a matching score based on that elevation value
			int numberOfClosedPoints = 0;
			for (AbstractImport imp : element.getMatchingImports()) {
				int z = (int) Math.round(((ElevationImport) imp).z);
				if (z >= elevation - toleranceDelta) {
					numberOfClosedPoints++;
				}
			}
			// The matching score is the coverage of closest points
			element.setMatchingScore((float) numberOfClosedPoints / element.getMatchingImports().size());
			LOGGER.info("For elevation=" + elevation + " the number of closed points is: " + numberOfClosedPoints
					+ " and the matching score is: " + element.getMatchingScore());
			if (element.getMatchingScore() >= minMatchingScore) {
				LOGGER.info("Ok it's enough, the value " + elevation + " can be used");
				break;
			}
		}

		// Check the DSM value is not outside the range of allowed values
		if (!this.checkElevationValue(altitude, ElevationDataSource.Use.DSM)) {
			LOGGER.warn("Skipping element because surface elevation is outside the range of allowed values");
			return 0;
		}

		// Store the final height of the building which is the elevation minus the altitude
		element.setComputedHeight(new Integer(elevation - altitude));
		LOGGER.info("Computed height is: " + element.getComputedHeight());

		return element.getMatchingScore();
	}

	@Override
	protected BuildingElement instanciateElement(long osmId) {
		return new BuildingElement(osmId);
	}

}
