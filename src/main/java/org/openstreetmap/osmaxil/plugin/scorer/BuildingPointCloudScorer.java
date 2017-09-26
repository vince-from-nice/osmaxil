package org.openstreetmap.osmaxil.plugin.scorer;

import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.CloudPointImport;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("BuildingPointCloudScorer") @Lazy
public class BuildingPointCloudScorer extends AbstractElementScorer<BuildingElement> {
	
	@Value("${scorer.buildingPointCloud.minMatchingPoints}")
	public int minMatchingPoints;
	
	@Value("${scorer.buildingPointCloud.computingDistance}")
	public int computingDistance;
	
	@Value("${scorer.buildingPointCloud.toleranceDelta}")
	public float toleranceDelta;
	
	public float computeElementMatchingScore(BuildingElement element, float minMatchingScore) {
		element.setMatchingScore(0);
		
		LOGGER.info("The number of total matching points is: " + element.getMatchingImports().size());
	
		// Check if the total of matching imports is fine
        // TODO Move it to scorer
		if (element.getMatchingImports().size() < this.minMatchingPoints) {
			LOGGER.info("Element has only " + element.getMatchingImports().size()
					+ " matching imports, skipping it because minimum value is "
					+ this.minMatchingPoints);
			return 0;
		}
		
		// Compute altitude of the center of the building with the DTM 
		Coordinates center = this.osmPostgis.getPolygonCenter(
				(element.getRelationId() == null ? element.getOsmId() : - element.getRelationId()),
				this.genericRasterFile.getSrid());
		int altitude = (int) Math.round(this.genericRasterFile.getValueByCoordinates(Double.parseDouble(center.x), Double.parseDouble(center.y), this.osmPostgis.getSrid()));
		LOGGER.info("Computed altitude is: " + altitude);
		
		// Find the max of all points elevations
		int max = 0;
		for (AbstractImport imp : element.getMatchingImports()) {
			int h = (int) Math.round(Double.parseDouble(((CloudPointImport) imp).getZ()));
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
				int z = (int) Double.parseDouble(((CloudPointImport) imp).getZ());
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
		
		// Store the final height of the building which is the elevation minus the altitude
		element.setComputedHeight(new Integer(elevation - altitude));
		LOGGER.info("Computed height is: " + element.getComputedHeight());
					
		return element.getMatchingScore();
	}

}
