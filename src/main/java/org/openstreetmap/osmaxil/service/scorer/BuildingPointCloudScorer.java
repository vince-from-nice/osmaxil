package org.openstreetmap.osmaxil.service.scorer;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.GenericRasterFile;
import org.openstreetmap.osmaxil.dao.OsmPostgisDB;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.CloudPointImport;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("BuildingPointCloudScorer") @Lazy
public class BuildingPointCloudScorer {

    @Autowired
    protected OsmPostgisDB osmPostgis;
    
	@Autowired
	protected GenericRasterFile genericRasterFile;
	
	static protected final Logger LOGGER = Logger.getLogger(Application.class);
	
	public float computeElementMatchingScore(BuildingElement element, int computingDistance, float toleranceDelta, float minMatchingScore) {
		LOGGER.info("The number of total matching points is: " + element.getMatchingImports().size());

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
