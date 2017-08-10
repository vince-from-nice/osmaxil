package org.openstreetmap.osmaxil.dao;

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
public class OsmPostgisDB {
    
    @Autowired
    @Qualifier("osmPostgisJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    @Value("${osmPostgis.srid}")
    private int srid;
    
    // TODO Value the SRID in settings.xml
    private static int SRID_FOR_AREA_COMPUTATION = 32633;
    
    static private final Logger LOGGER = Logger.getLogger(Application.class);
    
    public Long[] findClosestElementIdsByQuery(String query) {
        //TODO
        return null;
    }

    public Long[] findElementIdsByQuery(String query) {
        List<Long> result = this.jdbcTemplate.query(
                query,
                new RowMapper<Long>() {
                    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return Long.parseLong(rs.getString("osm_id"));
                    }
                });
        return result.toArray(new Long[result.size()]);
    }
    
    public class IdWithScore {
        public long id;
        public double score;
    }

    public IdWithScore[] findElementIdsWithScoreByQuery(String query) {
        List<IdWithScore> result = this.jdbcTemplate.query(
                query,
                new RowMapper<IdWithScore>() {
                    public IdWithScore mapRow(ResultSet rs, int rowNum) throws SQLException {
                        IdWithScore idWithScore = new IdWithScore();
                        idWithScore.id = Long.parseLong(rs.getString("osm_id"));
                        idWithScore.score = Double.parseDouble(rs.getString("distance"));
                        return idWithScore;
                    }
                });
        return result.toArray(new IdWithScore[result.size()]);
    }
    
    public class IdWithGeom {
        public long id;
        public String geom;
    }

    public IdWithGeom[] findElementIdsWithGeomByQuery(String query) {
        List<IdWithGeom> result = this.jdbcTemplate.query(
                query,
                new RowMapper<IdWithGeom>() {
                    public IdWithGeom mapRow(ResultSet rs, int rowNum) throws SQLException {
                    	IdWithGeom idWithGeom = new IdWithGeom();
                        idWithGeom.id = Long.parseLong(rs.getString("osm_id"));
                        idWithGeom.geom = rs.getString("geomAsWKT");
                        return idWithGeom;
                    }
                });
        return result.toArray(new IdWithGeom[result.size()]);
    }
    
    public String getRelationMembers(long relationId) {
        String result = "";
        String query = "select members from planet_osm_rels where id= ? ;";
        result = this.jdbcTemplate.queryForObject(query, String.class, relationId);
        return result;
    }
    
    public int getPolygonAreaById(long osmId) {
        int result = 0;
        String query = "select ST_Area(ST_Transform(way, " + SRID_FOR_AREA_COMPUTATION + ")) from planet_osm_polygon where osm_id = ?";
        LOGGER.debug("Computing area of polygon " + osmId + " with query: " + query);
        result = this.jdbcTemplate.queryForObject(query, Integer.class, osmId);
        return result;
    }
    
    public Coordinates getPolygonCenter(long osmId, int targetSrid) {
    	String query = "select ST_X(ST_Centroid(way)) as x, ST_Y(ST_Centroid(way)) as y from planet_osm_polygon where osm_id = ?";
    	if (targetSrid != this.getSrid()) {
    		query = "select ST_X(center) as x, ST_Y(center) as y from (select ST_Transform(ST_Centroid(way), 2154) as center from planet_osm_polygon where osm_id = ?) a";
    	}
    	LOGGER.debug("Computing center of polygon with query: " + query);
        Coordinates result = this.jdbcTemplate.queryForObject(
                query,
                new RowMapper<Coordinates>() {
                    public Coordinates mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Coordinates(rs.getString("x"), rs.getString("y"), "0");
                    }
                }, osmId);
        return result;
    }
    
    public int getSrid() {
        return srid;
    }
    
}
