package org.openstreetmap.osmaxil.plugin.enhancer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.dao.GenericPostgisDB;
import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.BuildingImport;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractElementMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractImportMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.BuildingElementMatcher;
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

	@Autowired
	protected GenericPostgisDB genericPostgis;

	@Autowired
	protected BuildingElementMatcher buildingElementMatcher;

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

	public void displayLoadingStatistics() {
		LOGGER_FOR_STATS.info("=== Loading statistics ===");
		LOGGER_FOR_STATS.info("Sorry but there is no loading statistic for that plugin ");
	}

	@Override
	protected String getExistingElementQuery() {
		return "SELECT osm_id, 1 from planet_osm_polygon WHERE building <> ''";
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
	protected AbstractElementMatcher<BuildingElement> getElementMatcher() {
		return this.buildingElementMatcher;
	}

	@Override
	protected AbstractMatchingScorer<BuildingElement> getScorer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getChangesetComment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getChangesetSourceLabel() {
		// TODO Auto-generated method stub
		return null;
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
