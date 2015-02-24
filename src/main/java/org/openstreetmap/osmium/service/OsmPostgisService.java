package org.openstreetmap.osmium.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class OsmPostgisService {
    
    @Autowired
    @Qualifier("postgisJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

//  @Autowired
//  @Qualifier("postgisJdbcTemplate")
//  private DataSource dataSource;

    public Long[] findElementIdsByQuery(String query) {
        List<Long> result = this.getJdbcTemplate().query(
                query,
                new RowMapper<Long>() {
                    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return Long.parseLong(rs.getString("osm_id"));
                    }
                });
        return result.toArray(new Long[result.size()]);
    }
    
    public int getElementAreaById(long osmId) {
        int result = 0;
        String query = "select ST_Area(way) from planet_osm_polygon where osm_id = ? ;";
        result = this.getJdbcTemplate().queryForObject(query, Integer.class, osmId);
        return result;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
