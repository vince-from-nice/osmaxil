package org.openstreetmap.osmaxil.plugin.enhancer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.dao.GenericPostgisDB;
import org.openstreetmap.osmaxil.dao.OsmPostgisDB;
import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.BuildingImport;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.model.misc.StringCoordinates;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractImportMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractImportParser;
import org.openstreetmap.osmaxil.plugin.common.scorer.AbstractMatchingScorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("NiceBuildingEnhancer")
public class NiceBuildingEnhancer extends AbstractEnhancerPlugin<BuildingElement, BuildingImport> {

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

	@Value("${plugins.niceBuildingEnhancer.changesetSourceLabel}")
	private String changesetSourceLabel;

	@Value("${plugins.niceBuildingEnhancer.changesetComment}")
	private String changesetComment;

	private static final String UPDATABLE_TAG_NAMES[] = new String[] { ElementTag.BUILDING_LEVELS };

	private static final String MATCHING_TAG_NAME = ElementTag.BUILDING_LEVELS;

	@Autowired
	protected GenericPostgisDB genericPostgis;

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
		String query = "SELECT osm_id, ST_AsEWKT(way) as geomAsWKT, 1 from planet_osm_polygon WHERE building <> ''";
		if (query.toUpperCase().indexOf(" WHERE ") == -1) {
			query += " WHERE " + condition;
		} else {
			query += " AND " + condition;
		}
		LOGGER.debug("Used query is: " + query);
		OsmPostgisDB.IdWithGeom[] idsWithGeom = this.osmPostgis.findElementIdsWithGeomByQuery(query);
		for (OsmPostgisDB.IdWithGeom idWithGeom : idsWithGeom) {
			BuildingElement element = new BuildingElement(idWithGeom.id);
			element.setGeometryString(idWithGeom.geom);
			results.add(element);
		}
		LOGGER.debug("Number of returned element: " + results.size());
		return results;
	}

	@Override
	public List<BuildingImport> findMatchingImports(BuildingElement element, int srid) {
		List<BuildingImport> result = new ArrayList<BuildingImport>();
		List<StringCoordinates> data = new ArrayList<>();
		// Find in PostGIS all imports matching (ie. containing) the element
		if (element.getGeometryString() == null) {
			LOGGER.warn("Unable to find matching imports because element has no geometry string");
			return result;
		}
		data = this.genericPostgis.findPointByGeometry(element.getGeometryString(), srid);
		for (StringCoordinates stringCoordinates : data) {
			BuildingImport imp = new BuildingImport();
			List<StringCoordinates> coordinates = new ArrayList<>();
			coordinates.add(stringCoordinates);
			imp.setCoordinates(coordinates);
		}
		return result;
	}

	@Override
	protected void computeMatchingScores(BuildingElement element) {
		// TODO
	}

	@Override
	protected boolean isElementTagUpdatable(BuildingElement element, String tagName) {
		// Building tags are updatable only if it doesn't have an original value
		return element.getOriginalValuesByTagNames().get(tagName) == null;
	}

	public void displayLoadingStatistics() {
		LOGGER_FOR_STATS.info("=== Loading statistics ===");
		LOGGER_FOR_STATS.info("Sorry but there is no loading statistic for that plugin ");
	}

	@Override
	protected AbstractImportParser<BuildingImport> getParser() {
		// useless for this plugin
		return null;
	}

	@Override
	protected AbstractImportMatcher<BuildingImport> getMatcher() {
		// useless for this plugin
		return null;
	}

	@Override
	protected AbstractMatchingScorer<BuildingElement> getScorer() {
		// useless for this plugin
		return null;
	}

	@Override
	protected boolean updateElementTag(BuildingElement element, String tagName) {
		// TODO Auto-generated method stub
		return false;
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
