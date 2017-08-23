package org.openstreetmap.osmaxil.plugin.enhancer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.dao.GenericRasterFile;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("NiceBuildingEnhancer") @Lazy
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
	
	@Value("${plugins.niceBuildingEnhancer.computingDistance}")
	private int computingDistance;
	
	@Value("${plugins.niceBuildingEnhancer.toleranceDelta}")
	private float toleranceDelta;
	
	@Value("${plugins.niceBuildingEnhancer.shrinkRadius}")
	private int shrinkRadius;

	@Value("${plugins.niceBuildingEnhancer.changesetSourceLabel}")
	private String changesetSourceLabel;

	@Value("${plugins.niceBuildingEnhancer.changesetComment}")
	private String changesetComment;

	private static final String UPDATABLE_TAG_NAMES[] = new String[] { ElementTag.HEIGHT };

	@Autowired
	protected GenericPostgisDB genericPostgis;
	
	@Autowired
	protected GenericRasterFile genericDemFile;
	
	private Map<BuildingElement, Float>computedHeightsByBuilding = new HashMap<>();
	
	private Map<Long, Integer> outerMemberIndexes = new HashMap<>(); // used by building relations

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
		// Get only elements whose coordinates are fine with the filtering areas
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
		// Specify building IDs directly (used for debugging)
		query = "SELECT osm_id, ST_AsText(way) AS geomAsWKT, 1 FROM planet_osm_polygon WHERE osm_id = -6640171";
		LOGGER.debug("Used query is: " + query);
		// Fetch from DB the IDs and the geometries
		OsmPostgisDB.IdWithString[] idsWithGeom = this.osmPostgis.findElementIdsWithGeomByQuery(query);
		for (OsmPostgisDB.IdWithString idWithGeom : idsWithGeom) {
			// If ID is negative it means the element is normal (ie. a way)
			if (idWithGeom.id > 0) {
				BuildingElement element = new BuildingElement(idWithGeom.id);
				element.setGeometryString(idWithGeom.string);
				results.add(element);
			} 
			// If ID is negative it means the element is a relation
			// See http://wiki.openstreetmap.org/wiki/Osm2pgsql/schema for details
			else {
				long relationId = -idWithGeom.id;
				String membersString = osmPostgis.getRelationMembers(relationId);
				LOGGER.info("A multipolygon relation has been found (" + idWithGeom.id + "), its outer members are: " + membersString); 
				List<Long> outerMemberIds = BuildingElement.getOuterOrInnerMemberIds(relationId, membersString, true);
				Integer currentOuterMemberIndex = this.outerMemberIndexes.get(relationId);
				if (currentOuterMemberIndex == null) {
					currentOuterMemberIndex = 0;
				}
				long outerMemberId = outerMemberIds.get(currentOuterMemberIndex);
				this.outerMemberIndexes.put(relationId, ++currentOuterMemberIndex);
				LOGGER.info("Outer member ID found is " + outerMemberId + " (current index is " + currentOuterMemberIndex + "), creating a new element with it");
				BuildingElement element = new BuildingElement(outerMemberId);
				element.setRelationId(relationId);
				element.setGeometryString(idWithGeom.string);
				results.add(element);
			}
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
		LOGGER.info("The number of total matching points is: " + element.getMatchingImports().size());
		// Check there is enough matching points
		if (element.getMatchingImports().size() < this.minMatchingPoints) {
			LOGGER.info("Element has only " + element.getMatchingImports().size() + " matching import(s), skipping it !");
			return;
		}

		// Compute altitude of the center of the building from the DTM of Nice ( 
		Coordinates center = this.osmPostgis.getPolygonCenter(
				(element.getRelationId() == null ? element.getOsmId() : - element.getRelationId()),
				this.genericDemFile.getSrid());
		int altitude = (int) Math.round(this.genericDemFile.getValueByCoordinates(Double.parseDouble(center.x), Double.parseDouble(center.y), this.osmPostgis.getSrid()));
		LOGGER.info("Computed altitude is: " + altitude);
		
		// Find the max of all points elevations
		int max = 0;
		for (AbstractImport imp : element.getMatchingImports()) {
			int h = (int) Math.round(Double.parseDouble(((PointImport) imp).getZ()));
			if (h > max) {
				max = h;
			}
		}
		LOGGER.info("Max elevation is: " + max);

		// Decrement elevation from the max until a descent value is found.
		// The goal is to find the *highest* elevation which has a valid score.
		int elevation = max;
		for (elevation = max; elevation > max - this.computingDistance && elevation > altitude; elevation--) {
			// Compute a matching score based on that elevation value
			int numberOfClosedPoints = 0;
			for (AbstractImport imp : element.getMatchingImports()) {
				int z = (int) Double.parseDouble(((PointImport) imp).getZ());
				if (z >= elevation - this.toleranceDelta) {
					numberOfClosedPoints++;	
				}			
			}
			// The matching score is the coverage of closest points 
			element.setMatchingScore((float) numberOfClosedPoints / element.getMatchingImports().size());
			LOGGER.info("For height=" + elevation + " the number of closed points is: " + numberOfClosedPoints 
					+ " and the matching score is: " + element.getMatchingScore());
			if (element.getMatchingScore() >= this.minMatchingScore) {
				LOGGER.info("Ok it's enough, the value " + elevation + " can be used");
				break;
			}
		}
		
		// The final height is the elevation minus the altitude
		Float height = new Float(elevation - altitude);
		this.computedHeightsByBuilding.put(element, height);
		LOGGER.info("Computed height is: " + height);
	}

	@Override
	protected boolean isElementTagUpdatable(BuildingElement element, String tagName) {
		// Building tags are updatable only if it doesn't have an original value
		return element.getOriginalValuesByTagNames().get(tagName) == null;
	}
	
	@Override
	protected boolean updateElementTag(BuildingElement element, String tagName) {
        boolean updated = false;
        if (ElementTag.HEIGHT.equals(tagName)) {
            Float computedHeight = this.computedHeightsByBuilding.get(element);
            if (computedHeight == null) {
                LOGGER.warn("Cannot update tag because computed height is null");
                return false;
            }
            element.setHeight(computedHeight);
            LOGGER.info("===> Updating height to [" + computedHeight + "]");
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
        LOGGER_FOR_STATS.info("Specific settings of the plugin:");
        LOGGER_FOR_STATS.info(" - Minimum matching point is: " + this.minMatchingPoints);
        LOGGER_FOR_STATS.info(" - Shrink radius is: " + this.shrinkRadius);
        LOGGER_FOR_STATS.info(" - Tolerance delta is: " + this.toleranceDelta);
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
        long innerMemberId = BuildingElement.getOuterOrInnerMemberIds(relationId, membersString, false).get(0);
        String query = "SELECT osm_id, ST_AsText(way) AS geomAsWKT, 1 FROM planet_osm_polygon WHERE building <> '' AND osm_id=" + innerMemberId;
        LOGGER.debug("Used query is: " + query);
        // Damned the inner member id is a key of the planet_osm_ways table which doesn't store the coordinates but only point IDs :(
        //OsmPostgisDB.IdWithGeom[] innerMemberIdWithGeom = this.osmPostgis.findElementIdsWithGeomByQuery(query);
        // Need to do another request to fetch points coordinates...
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
