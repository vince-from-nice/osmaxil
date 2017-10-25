package org.openstreetmap.osmaxil.plugin.loader;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.ElevationDatabase;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractElevationDbLoader {

	@Value("${loader.folderPath}")
	protected String folderPath;

	@Value("${loader.separator}")
	protected String separator;

	@Value("${loader.srid}")
	protected String srid;

	protected ElevationDatabase database;

	static protected final Logger LOGGER = Logger.getLogger(Application.class);

	abstract protected void loadData(String source) throws IOException;

	public void load(ElevationDatabase dataSource, String source) throws IOException {
		this.database = dataSource;
		LOGGER.info("Recreate the point cloud table from scratch.");
		this.recreatePointCloudTable(source);
		this.loadData(source);
		this.finalizePointCloudTable(source);
	}

	/**
	 * Obsolete method for point cloud table creation (COPY is so much faster
	 * compared to INSERT)
	 */
	protected void addPoint(String tableName, long id, String fileSrid, double x, double y, double z) {
		LOGGER.debug("Add point(" + x + " " + y + ") with z=" + z);
		// this.jdbcTemplate.execute("INSERT INTO " + tableName + " VALUES(" + id + ",
		// ST_Transform(ST_GeomFromText('POINT(" + x + " " + y + " " + z + ")', " +
		// fileSrid + "), " + this.srid + "))");
	}

	/**
	 * Recreate a point cloud table.
	 */
	protected void recreatePointCloudTable(String tableName) {
		this.database.executeSQL("DROP INDEX IF EXISTS geom_idx_for_" + tableName);
		this.database.executeSQL("DROP TABLE IF EXISTS " + tableName);
		this.database.executeSQL("CREATE TABLE " + tableName + " (x numeric(11,3), y numeric(11,3), z numeric(11,3))");
	}

	/**
	 * Fill a point cloud table by using the COPY statement (which much more
	 * efficient than INSERT).
	 */
	protected void copyPointCloudFromXYZFile(String tableName, String filePath) {
		this.database.executeSQL(
				"COPY " + tableName + " (x, y, z) FROM '" + filePath + "' WITH DELIMITER AS '" + this.separator + "'");
	}

	/**
	 * Finalize a point cloud table by adding its geometry column and an spatial
	 * index.
	 */
	protected void finalizePointCloudTable(String tableName) {
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

	/**
	 * Execute an external program.
	 */
	public static void executeCommand(String cmd) {
		try {
			LOGGER.info("Exec: " + cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			LOGGER.info("Process has returned: " + p.exitValue());
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

}
