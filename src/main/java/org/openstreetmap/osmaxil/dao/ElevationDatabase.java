package org.openstreetmap.osmaxil.dao;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ElevationDatabase {

    @Autowired
    @Qualifier("elevationPostgisJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    @Value("${elevationDatabase.srid}")
    private int srid;

	@Value("${elevationDatabase.xyzTableName}")
	private String xyzTableName;
	
	@Value("${elevationDatabase.xyzFolderPath}")
	private String xyzFolderPath;

	@Value("${elevationDatabase.xyzFileSrid}")
	private String xyzFileSrid;
	
	@Value("${elevationDatabase.shrinkRadius}")
	private int shrinkRadius;
    
    static private final Logger LOGGER = Logger.getLogger(Application.class);
    
    public void beginTransaction() {
    	LOGGER.debug("BEGIN TRANSACTION") ;
    	this.jdbcTemplate.execute("BEGIN TRANSACTION");
    }
    
    public void commitTransaction() {
    	LOGGER.debug("COMMIT TRANSACTION") ;
    	this.jdbcTemplate.execute("COMMIT TRANSACTION");
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Point cloud tables
    ////////////////////////////////////////////////////////////////////////////////
    
    public void createPointCloudTableFromXYZFiles() {
		LOGGER.info("Recreate the point cloud table from scratch.");
		this.recreatePointCloudTable(this.xyzTableName, this.xyzFileSrid);
		File xyzFolder = new File(this.xyzFolderPath);
		File[] xyzFiles = xyzFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xyz");
			}
		});
		for (int i = 0; i < xyzFiles.length; i++) {
			File xyzFile = xyzFiles[i];
			LOGGER.info("Loading file " + xyzFile);
			this.copyPointCloudFromYXZFile(this.xyzTableName, xyzFile.getPath());
		}
		this.finalizePointCloudTable(this.xyzTableName, this.xyzFileSrid);
    }
    
    /**
     * Find all points which intersect the including geometry and disjoint the excluding geometry (useful for multipolygon buildings with "hole").
     * That method use a radius as argument in order to skrink the including and excluding geometries.
     */
    public List<Coordinates> findPointByGeometry(String includingGeomAsWKT, String excludingGeomAsWKT, int geomSrid) {
    	final String includingGeom = "ST_GeomFromText('" + includingGeomAsWKT + "', " + geomSrid + ")";
    	String query = "SELECT x, y, z FROM " + this.xyzTableName;
    	String condition = "ST_Transform(ST_Buffer(" + includingGeom + ", -" + shrinkRadius + "), " + srid + ")";
    	query += " WHERE ST_Intersects(geom, " + condition + ")";
    	// Do the same for excluding geom (need cleanup before)
    	if (excludingGeomAsWKT != null) {
    		final String excludingGeom = "ST_GeomFromText('" + excludingGeomAsWKT + "', " + geomSrid + ")";
    		condition += " AND ST_Disjoint(geom, ST_Transform(ST_Buffer(" + excludingGeom + ", -" + shrinkRadius + "), " + srid + "))";
    	}
    	LOGGER.debug("Used query is: " + query);
    	List<Coordinates> results = this.jdbcTemplate.query(
                query,
                new RowMapper<Coordinates>() {
                    public Coordinates mapRow(ResultSet rs, int rowNum) throws SQLException {
                    	Coordinates coordinates = new Coordinates(rs.getString("x"), rs.getString("y"), rs.getString("z"));
                        return coordinates;
                    }
                });
    	return results;
    }
    
    /**
     * Find all points which intersect the including geometry and disjoint the excluding geometry (useful for multipolygon buildings with "hole").
     * That method use a factor as argument in order to scale/skrink the including and excluding geometries.
     * 
     * TODO: use JTS instead of PostGis functions (ST_Scale and ST_Translate) to perform the scale/skrink process because this implementation
     * is working but it takes 15 minutes to execute ! 
     */
    public List<Coordinates> findPointByGeometry(String includingGeomAsWKT, String excludingGeomAsWKT, float scaleFactor, int geomSrid) {
    	final String geom = "ST_Transform(ST_GeomFromText('" + includingGeomAsWKT + "', " + geomSrid + "), " + this.srid + ")";
    	String query = "SELECT x, y, z FROM " + this.xyzTableName + ", " + geom + " as includingGeom";
    	String condition = "ST_Scale(includingGeom, " + scaleFactor + ", " + scaleFactor + ")";
		condition = "ST_Translate(" + condition + ", " 
				+ "-" + scaleFactor + "*(ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2 + ((ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2), "
				+ "-" + scaleFactor + "*(ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2 + ((ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2))";
    	query += " WHERE ST_Intersects(geom, " + condition + ")";
    	// TODO do the same for excluding geom (need cleanup before)
//    	if (excludingGeomAsWKT != null) {
//    		condition += " AND ST_Disjoint(geom, ST_Transform(ST_GeomFromText('" + excludingGeomAsWKT + "', " + geomSrid + "), " + this.srid + "))";
//    	}
    	LOGGER.debug("Used query is: " + query);
    	List<Coordinates> results = this.jdbcTemplate.query(
                query,
                new RowMapper<Coordinates>() {
                    public Coordinates mapRow(ResultSet rs, int rowNum) throws SQLException {
                    	Coordinates coordinates = new Coordinates(rs.getString("x"), rs.getString("y"), rs.getString("z"));
                        return coordinates;
                    }
                });
    	return results;
    }
    
    /**
     * Obsolete method for point cloud table creation (COPY is so much faster compared to INSERT)
     */
    public void addPoint(String tableName, long id, String fileSrid, double x, double y, double z) {
    	LOGGER.debug("Add point(" + x + " " + y + ") with z=" + z) ;
    	this.jdbcTemplate.execute("INSERT INTO " + tableName + " VALUES(" + id +
    			", ST_Transform(ST_GeomFromText('POINT(" + x + " " + y + " " + z + ")', " + fileSrid + "), " + this.srid + "))");
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Private methods
    ////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Recreate a point cloud table.
     */
    private void recreatePointCloudTable(String tableName, String fileSrid) {
    	this.jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
    	this.jdbcTemplate.execute("CREATE TABLE " + tableName + " (x numeric(11,3), y numeric(11,3), z numeric(11,3))");
    }
    
    /**
     * Fill a point cloud table by using the COPY statement (which much more efficient than INSERT).
     */
    private void copyPointCloudFromYXZFile(String tableName, String filePath) {
    	this.jdbcTemplate.execute("COPY " + tableName + " (x, y, z) FROM '" + filePath + "' WITH DELIMITER AS ' '");    	
    }
    
    /**
     * Finalize a point cloud table by adding its geometry column and an spatial index.
     */
    private void finalizePointCloudTable(String tableName, String fileSrid) {
    	LOGGER.info("Add geometry column to the point cloud table");
    	this.jdbcTemplate.execute("SELECT AddGeometryColumn ('"+ tableName +"', 'geom', " + this.srid + ", 'POINT', 3)");
    	LOGGER.info("Update the geometry column of the point cloud table");
    	this.jdbcTemplate.execute("UPDATE " + tableName +
    			" SET geom = ST_Transform(ST_GeomFromText('POINT('||x||' '||y||' '||z||')', " + fileSrid + "), " + this.srid + ")");
    	LOGGER.info("Create an index on the geometry column of the point cloud table");
    	this.jdbcTemplate.execute("CREATE INDEX point_cloud_geom ON " + tableName + " USING GIST (geom)");   	
    }
    
}
