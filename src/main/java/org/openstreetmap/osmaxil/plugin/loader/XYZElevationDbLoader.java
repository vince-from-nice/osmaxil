package org.openstreetmap.osmaxil.plugin.loader;

import java.io.File;
import java.io.FilenameFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("XYZElevationDbLoader")
@Lazy
@Scope("prototype")
public class XYZElevationDbLoader extends AbstractElevationDbLoader {

	@Value("${loader.xyz.folderPath}")
	private String folderPath;

	@Value("${loader.xyz.separator}")
	private String separator;

	@Value("${loader.xyz.srid}")
	private String srid;

	public void loadData(String source) {
		LOGGER.info("Recreate the point cloud table from scratch.");
		this.recreatePointCloudTable(source);
		File xyzFolder = new File(this.folderPath);
		File[] xyzFiles = xyzFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xyz");
			}
		});
		for (int i = 0; i < xyzFiles.length; i++) {
			File xyzFile = xyzFiles[i];
			LOGGER.info("Loading file " + xyzFile);
			this.copyPointCloudFromYXZFile(source, xyzFile.getAbsolutePath());
		}
		this.finalizePointCloudTable(source);
	}

	/**
	 * Obsolete method for point cloud table creation (COPY is so much faster
	 * compared to INSERT)
	 */
	public void addPoint(String tableName, long id, String fileSrid, double x, double y, double z) {
		LOGGER.debug("Add point(" + x + " " + y + ") with z=" + z);
		// this.jdbcTemplate.execute("INSERT INTO " + tableName + " VALUES(" + id + ",
		// ST_Transform(ST_GeomFromText('POINT(" + x + " " + y + " " + z + ")', " +
		// fileSrid + "), " + this.srid + "))");
	}

	////////////////////////////////////////////////////////////////////////////////
	// Private methods
	////////////////////////////////////////////////////////////////////////////////

	/**
	 * Recreate a point cloud table.
	 */
	private void recreatePointCloudTable(String tableName) {
		this.database.executeSQL("DROP TABLE IF EXISTS " + tableName);
		this.database.executeSQL("CREATE TABLE " + tableName + " (x numeric(11,3), y numeric(11,3), z numeric(11,3))");
	}

	/**
	 * Fill a point cloud table by using the COPY statement (which much more
	 * efficient than INSERT).
	 */
	private void copyPointCloudFromYXZFile(String tableName, String filePath) {
		this.database.executeSQL("COPY " + tableName + " (x, y, z) FROM '" + filePath + "' WITH DELIMITER AS '"
				+ this.separator + "'");
	}

	/**
	 * Finalize a point cloud table by adding its geometry column and an spatial
	 * index.
	 */
	private void finalizePointCloudTable(String tableName) {
		LOGGER.info("Add geometry column to the point cloud table");
		this.database.executeSQL(
				"SELECT AddGeometryColumn ('" + tableName + "', 'geom', " + this.database.getSrid() + ", 'POINT', 3)");
		LOGGER.info("Update the geometry column of the point cloud table");
		this.database.executeSQL(
				"UPDATE " + tableName + " SET geom = ST_Transform(ST_GeomFromText('POINT('||x||' '||y||' '||z||')', "
						+ this.srid + "), " + this.database.getSrid() + ")");
		LOGGER.info("Create an index on the geometry column of the point cloud table");
		this.database.executeSQL("CREATE INDEX geom_idx_for_" + tableName + " ON " + tableName + " USING GIST (geom)");
	}

}
