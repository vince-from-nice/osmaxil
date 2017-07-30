package org.openstreetmap.osmaxil.dao;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class GenericPostgis {

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
    	this.jdbcTemplate.execute("SELECT AddGeometryColumn ('"+ tableName +"', 'geom', " + this.srid + ", 'POINT', 3)");
    	this.jdbcTemplate.execute("UPDATE " + tableName +
    			" SET geom = ST_Transform(ST_GeomFromText('POINT('||x||' '||y||' '||z||')', " + fileSrid + "), " + this.srid + ")");
   	
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
