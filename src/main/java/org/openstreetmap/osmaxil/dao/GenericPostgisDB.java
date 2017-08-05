package org.openstreetmap.osmaxil.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmPostgisDB.IdWithGeom;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class GenericPostgisDB {

    @Autowired
    @Qualifier("genericPostgisJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    @Value("${genericPostgis.srid}")
    private int srid;
    
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
    
    public void recreatePointCloudTable(String tableName, String fileSrid) {
    	this.jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
    	this.jdbcTemplate.execute("CREATE TABLE " + tableName + " (x numeric(11,3), y numeric(11,3), z numeric(11,3))");
    }
    
    public void copyPointCloudFromYXZFile(String tableName, String filePath) {
    	this.jdbcTemplate.execute("COPY " + tableName + " (x, y, z) FROM '" + filePath + "' WITH DELIMITER AS ' '");    	
    }
    
    public void finalizePointCloudTable(String tableName, String fileSrid) {
    	LOGGER.info("Add geometry column to the point cloud table");
    	this.jdbcTemplate.execute("SELECT AddGeometryColumn ('"+ tableName +"', 'geom', " + this.srid + ", 'POINT', 3)");
    	LOGGER.info("Update the geometry column of the point cloud table");
    	this.jdbcTemplate.execute("UPDATE " + tableName +
    			" SET geom = ST_Transform(ST_GeomFromText('POINT('||x||' '||y||' '||z||')', " + fileSrid + "), " + this.srid + ")");
    	LOGGER.info("Create an index on the geometry column of the point cloud table");
    	this.jdbcTemplate.execute("CREATE INDEX point_cloud_geom ON " + tableName + " USING GIST (geom)");   	
    }
    
    public List<Coordinates> findPointByGeometry(String includingGeomAsWKT, String excludingGeomAsWKT, float scaleFactor, int geomSrid) {
    	final String geom = "ST_Transform(ST_GeomFromText('" + includingGeomAsWKT + "', " + geomSrid + "), " + this.srid + ")";
    	String query = "SELECT x, y, z FROM point_cloud_of_nice, " + geom + " as includingGeom";
    	String condition = "ST_Scale(includingGeom, " + scaleFactor + ", " + scaleFactor + ")";
		condition = "ST_Translate(" + condition + ", " 
				+ "-" + scaleFactor + "*(ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2 + ((ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2), "
				+ "-" + scaleFactor + "*(ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2 + ((ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2))";
    	query += " WHERE ST_Intersects(geom, " + condition + ")";
    	// TODO do the same for excluding geom (need cleanup before)
//    	if (excludingGeomAsWKT != null) {
//    		condition += " AND ST_Disjoint(geom, ST_Transform(ST_GeomFromText('" + excludingGeomAsWKT + "', " + geomSrid + "), " + this.srid + "))";
//    	}
    	// TODO use JTS to scale the geoms ? The current query is working but takes 15 minutes to execute :(
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
    
}
