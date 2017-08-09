package org.openstreetmap.osmaxil.plugin.enhancer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.dao.GenericDemFile;
import org.openstreetmap.osmaxil.dao.GenericPostgisDB;
import org.openstreetmap.osmaxil.dao.OsmPostgisDB;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.model.PointImport;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractImportMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractImportParser;
import org.openstreetmap.osmaxil.plugin.common.scorer.AbstractMatchingScorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("NiceBuildingEnhancer")
public class NiceBuildingEnhancer extends AbstractEnhancerPlugin<BuildingElement, PointImport> {

	@Value("${plugins.niceBuildingEnhancer.xyzFolderPath}")
	private String xyzFolderPath;

	@Value("${plugins.niceBuildingEnhancer.xyzFileSrid}")
	private String xyzFileSrid;

	@Value("${plugins.niceBuildingEnhancer.needToPreparePointCloudInDB}")
	private Boolean needToPreparePointCloudInDB;

	@Value("${plugins.niceBuildingEnhancer.pointCloudTableName}")
	private String pointCloudTableName;

	@Value("${plugins.niceBuildingEnhancer.minMatchingScore}")
	private float minMatchingScore;
	
	@Value("${plugins.niceBuildingEnhancer.minMatchingPoints}")
	private int minMatchingPoints;
	
	@Value("${plugins.niceBuildingEnhancer.toleranceRadius}")
	private float toleranceRadius;
	
	@Value("${plugins.niceBuildingEnhancer.shrinkRadius}")
	private int shrinkRadius;

	@Value("${plugins.niceBuildingEnhancer.changesetSourceLabel}")
	private String changesetSourceLabel;

	@Value("${plugins.niceBuildingEnhancer.changesetComment}")
	private String changesetComment;

	private static final String UPDATABLE_TAG_NAMES[] = new String[] { ElementTag.BUILDING_LEVELS };

	private static final String MATCHING_TAG_NAME = ElementTag.BUILDING_LEVELS;

	@Autowired
	protected GenericPostgisDB genericPostgis;
	
	@Autowired
	protected GenericDemFile genericDemFile;

	// =========================================================================
	// Overrided methods
	// =========================================================================

	@Override
	public void load() {
		if (needToPreparePointCloudInDB) {
			LOGGER.info("Recreate the point cloud table from scratch.");
			this.genericPostgis.recreatePointCloudTable(this.pointCloudTableName, this.xyzFileSrid);
			File xyzFolder = new File(this.xyzFolderPath);
			File[] xyzFiles = xyzFolder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xyz");
				}
			});
			for (int i = 0; i < xyzFiles.length; i++) {
				File xyzFile = xyzFiles[i];
				LOGGER.info("Loading file " + xyzFile);
				this.genericPostgis.copyPointCloudFromYXZFile(this.pointCloudTableName, xyzFile.getPath());
			}
			this.genericPostgis.finalizePointCloudTable(this.pointCloudTableName, this.xyzFileSrid);
		} else {
			LOGGER.info("Point cloud table is not recreated, use the existing table.");
		}
	}
	
	protected List<BuildingElement> getTargetedElements() {
		List<BuildingElement> results = new ArrayList<>();
		String condition = "ST_Intersects(way, ST_Transform(ST_GeomFromText('" + includingAreaString + "', " + this.filteringAreaSrid + "), "
				+ osmPostgis.getSrid() + "))";
		condition += " AND ST_Disjoint(way, ST_Transform(ST_GeomFromText('" + excludingAreaString + "', " + this.filteringAreaSrid + "), "
				+ osmPostgis.getSrid() + "))";
		String query = "SELECT osm_id, ST_AsText(way) AS geomAsWKT, 1 FROM planet_osm_polygon WHERE building <> ''";
		if (query.toUpperCase().indexOf(" WHERE ") == -1) {
			query += " WHERE " + condition;
		} else {
			query += " AND " + condition;
		}
		LOGGER.debug("Used query is: " + query);
		OsmPostgisDB.IdWithGeom[] idsWithGeom = this.osmPostgis.findElementIdsWithGeomByQuery(query);
		for (OsmPostgisDB.IdWithGeom idWithGeom : idsWithGeom) {
			BuildingElement element = new BuildingElement(idWithGeom.id);
			// If ID is negative it means the element is a multipolygon relations => need to find its relevant outer member
			if (idWithGeom.id < 0) {
                LOGGER.debug("A multipolygon relation has been found (" + idWithGeom.id + "), looking for its relevant outer member");
                long relationId = - idWithGeom.id;
                element.setRelationId(relationId);
                String membersString = osmPostgis.getRelationMembers(relationId);
                element.setOsmId(BuildingElement.getOuterOrInnerMemberId(relationId, membersString, true));
                element.setInnerGeometryString(this.getInnerGeometryString(relationId, membersString));
			}
			element.setGeometryString(idWithGeom.geom);
			results.add(element);
		}
		LOGGER.info("Number of returned element: " + results.size());
		return results;
	}
	
	@Override
	public List<PointImport> findMatchingImports(BuildingElement element, int srid) {
		List<PointImport> result = new ArrayList<PointImport>();
		List<Coordinates> data = new ArrayList<>();
		// Find in PostGIS all imports matching (ie. containing) the element
		if (element.getGeometryString() == null) {
			LOGGER.warn("Unable to find matching imports because element has no geometry string");
			return result;
		}
		data = this.genericPostgis.findPointByGeometry(element.getGeometryString(), element.getInnerGeometryString(), shrinkRadius, srid);
		// Create imports from results
		for (Coordinates coordinates : data) {
			PointImport imp = new PointImport(coordinates);
			// Use the rounded value of 'z' as ID 
			imp.setId(Long.parseLong(coordinates.z.substring(0, coordinates.z.indexOf("."))));
			// TODO transform lat/lon to the correct SRID ? 
			imp.setLatitude(Double.parseDouble(coordinates.x));
			imp.setLongitude(Double.parseDouble(coordinates.y));
			result.add(imp);
			this.counterForMatchedImports++;
		}
		return result;
	}

	@Override
	protected void computeMatchingScores(BuildingElement element) {
		// Check there is enough matching points
		if (element.getMatchingImports().size() < this.minMatchingPoints) {
			LOGGER.info("Element has only " + element.getMatchingImports().size() + " matching import(s), skipping it !");
			return;
		}
		
		// Find the best value for building height
		int computedHeight = 0;
		// for now it is just the average of all points heights but a more complex statistic function would be better...
		for (AbstractImport imp : element.getMatchingImports()) {
			computedHeight += Double.parseDouble(((PointImport) imp).getZ());
		}
		computedHeight = computedHeight / element.getMatchingImports().size();
		LOGGER.info("Computed height is: " + computedHeight);
		
		// Compute matching score based on that height value and the tolerance radius
		int numberOfPointClosedToComputedHeight = 0;
		for (AbstractImport imp : element.getMatchingImports()) {
			int z = (int) Double.parseDouble(((PointImport) imp).getZ());
			if (z >= computedHeight - this.toleranceRadius && z <= computedHeight + this.toleranceRadius) {
				numberOfPointClosedToComputedHeight++;	
			}			
		}
		element.setMatchingScore((float) numberOfPointClosedToComputedHeight / element.getMatchingImports().size());
		
		// TODO Compute the coordinates of the building center
		double xCenter = 0;
		double yCenter = 0;
		
		// TODO Compute the altitude of the center of the building (thanks to GDAL and the DTM of Nice)
		double altitude = this.genericDemFile.getValueByCoordinates(xCenter, yCenter, this.osmPostgis.getSrid());
		LOGGER.info("Computed altitude is: " + altitude);
		
		// TODO Remove the altitude from the computed height
		
		// Log some infos
		LOGGER.info("The number of total matching points is: " + element.getMatchingImports().size());
		LOGGER.info("The number of points closed to computed height is: " + numberOfPointClosedToComputedHeight);
		LOGGER.info("The matching score is: " + element.getMatchingScore());
	}

	@Override
	protected boolean isElementTagUpdatable(BuildingElement element, String tagName) {
		// Building tags are updatable only if it doesn't have an original value
		return element.getOriginalValuesByTagNames().get(tagName) == null;
	}
	
	@Override
	protected boolean updateElementTag(BuildingElement element, String tagName) {
        boolean updated = false;
		// TODO
        String tagValue = "todo";
        if (ElementTag.HEIGHT.equals(tagName)) {
            element.setHeight(Float.parseFloat(tagValue));
            LOGGER.info("===> Updating height to [" + tagValue + "]");
            updated = true;
        }
        return updated;
	}

	public void displayLoadingStatistics() {
		LOGGER_FOR_STATS.info("=== Loading statistics ===");
		LOGGER_FOR_STATS.info("Sorry but there is no loading statistic for that plugin ");
	}
	
    @Override
    public void displayProcessingStatistics() {
        super.displayProcessingStatistics();
        LOGGER_FOR_STATS.info("Minimum matching score is: " + this.minMatchingScore);
        LOGGER_FOR_STATS.info("Minimum matching point is: " + this.minMatchingPoints);
        LOGGER_FOR_STATS.info("Shrink radius is: " + this.shrinkRadius);
        LOGGER_FOR_STATS.info("Tolerance radius is: " + this.toleranceRadius);
        LOGGER_FOR_STATS.info("Total of matching points: " + this.counterForMatchedImports);
		LOGGER_FOR_STATS.info("Average of matching points for each building: "
				+ this.counterForMatchedImports / this.matchedElements.size());
    }

	@Override
	protected AbstractImportParser<PointImport> getParser() {
		// useless for this plugin
		return null;
	}

	@Override
	protected AbstractImportMatcher<PointImport> getMatcher() {
		// useless for this plugin
		return null;
	}

	@Override
	protected AbstractMatchingScorer<BuildingElement> getScorer() {
		// useless for this plugin
		return null;
	}

	@Override
	protected BuildingElement instanciateElement(long osmId) {
		return new BuildingElement(osmId);
	}

	@Override
	protected String[] getUpdatableTagNames() {
		return UPDATABLE_TAG_NAMES;
	}

	@Override
	protected String getChangesetSourceLabel() {
		return changesetSourceLabel;
	}

	@Override
	protected String getChangesetComment() {
		return changesetComment;
	}

	@Override
	protected float getMinimalMatchingScore() {
		return this.minMatchingScore;
	}

	// =========================================================================
	// Private methods
	// =========================================================================

	/**
	 * Returns the geometry as WKT of the inner member of a multipolygon building.
	 */
	private String getInnerGeometryString(long relationId, String membersString) {
        long innerMemberId = BuildingElement.getOuterOrInnerMemberId(relationId, membersString, false);
        String query = "SELECT osm_id, ST_AsText(way) AS geomAsWKT, 1 FROM planet_osm_polygon WHERE building <> '' AND osm_id=" + innerMemberId;
        LOGGER.debug("Used query is: " + query);
        // Damned the inner member id is a key of the planet_osm_ways table which doesn't store the coordinates but only point IDs :(
        //OsmPostgisDB.IdWithGeom[] innerMemberIdWithGeom = this.osmPostgis.findElementIdsWithGeomByQuery(query);
        // TODO do another request to fetch points coordinates
        return null;
	}
	
	/**
	 * Former method to load all the point cloud data (but COPY is much more efficient).
	 */
	@Obsolete
	private void readPointCloudFromXYZFile(String xyzFilePath) {
		genericPostgis.beginTransaction();
		try (BufferedReader br = new BufferedReader(new FileReader(xyzFilePath))) {
			String line;
			String[] items;
			while ((line = br.readLine()) != null) {
				items = line.split(" ");
				if (items.length == 3) {
					genericPostgis.addPoint(this.pointCloudTableName, this.counterForParsedImports, this.xyzFileSrid,
							Double.parseDouble(items[0]), Double.parseDouble(items[1]), Double.parseDouble(items[2]));
				} else {
					LOGGER.warn("Line " + this.counterForParsedImports + " is invalid: " + line);
				}
				this.counterForParsedImports++;
			}
			genericPostgis.commitTransaction();
		} catch (java.lang.Exception e) {
			LOGGER.error("Unable to read XYZ file: ", e);
		} finally {
			LOGGER.info(LOG_SEPARATOR);
		}
	}
}
