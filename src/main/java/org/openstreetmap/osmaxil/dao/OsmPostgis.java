package org.openstreetmap.osmaxil.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class OsmPostgis {
    
    @Autowired
    @Qualifier("postgisJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    @Value("${postGis.srid}")
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
    
    public String getRelationMembers(long relationId) {
        String result = "";
        String query = "select members from planet_osm_rels where id= ? ;";
        result = this.jdbcTemplate.queryForObject(query, String.class, relationId);
        return result;
    }
    
    public int getPolygonAreaById(long osmId) {
        int result = 0;
        String query = "select ST_Area(ST_Transform(way, " + SRID_FOR_AREA_COMPUTATION + ")) from planet_osm_polygon where osm_id = ? ;";
        LOGGER.debug("Computing area of polygon " + osmId + " with query: " + query);
        result = this.jdbcTemplate.queryForObject(query, Integer.class, osmId);
        return result;
    }
    
    // TODO replace it by STS
    public String tranformGeometry(String wkt, int originalSrid) {
        String result = "";
        String query = "select ST_AsText(ST_Transform(ST_GeomFromText('" + wkt + "', ?), ?))";
        //LOGGER.debug("Transforming geometry with query: " + query);
        result = this.jdbcTemplate.queryForObject(query, String.class, originalSrid, this.srid);
        return result;
    }

    public int getSrid() {
        return srid;
    }
    
}
