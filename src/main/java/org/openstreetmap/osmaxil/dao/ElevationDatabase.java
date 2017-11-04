package org.openstreetmap.osmaxil.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openstreetmap.osmaxil.model.ElevationImport;
import org.openstreetmap.osmaxil.model.misc.Coordinates;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

//@Service("ElevationDatabase") @Scope("prototype") @Lazy
public class ElevationDatabase implements ElevationDataSource {

	// @Autowired @Qualifier("elevationPostgisJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	public int srid;

	private String tableName;

	public ElevationDatabase(String tableName, int srid, JdbcTemplate jdbcTemplate) {
		this.init(tableName, srid);
		this.jdbcTemplate = jdbcTemplate;
	}

	////////////////////////////////////////////////////////////////////////////////
	// Public overrided methods
	////////////////////////////////////////////////////////////////////////////////

	@Override
	public void init(String tableName, int srid) {
		this.tableName = tableName;
		this.srid = srid;
	}

	@Override
	public ElevationImport findElevationByCoordinates(float x, float y, float valueScale, int srid) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Find all points which intersect the including geometry and disjoint the excluding geometry (useful for multipolygon buildings with "hole").
	 * That method use a radius as argument in order to skrink the including and excluding geometries.
	 */
	@Override
	public List<ElevationImport> findAllElevationsByGeometry(String includingGeomAsWKT, String excludingGeomAsWKT, float valueScale, float shrinkRadius,
			int geomSrid) {
		final String includingGeom = "ST_GeomFromText('" + includingGeomAsWKT + "', " + geomSrid + ")";
		String query = "SELECT x, y, z FROM " + this.tableName;
		String condition = "ST_Transform(ST_Buffer(" + includingGeom + ", -" + shrinkRadius + "), " + srid + ")";
		query += " WHERE ST_Intersects(geom, " + condition + ")";
		// Do the same for excluding geom (need cleanup before)
		if (excludingGeomAsWKT != null) {
			final String excludingGeom = "ST_GeomFromText('" + excludingGeomAsWKT + "', " + geomSrid + ")";
			condition += " AND ST_Disjoint(geom, ST_Transform(ST_Buffer(" + excludingGeom + ", -" + shrinkRadius + "), " + srid + "))";
		}
		LOGGER.debug("Used query is: " + query);
		List<ElevationImport> results = this.jdbcTemplate.query(query, new RowMapper<ElevationImport>() {
			public ElevationImport mapRow(ResultSet rs, int rowNum) throws SQLException {
				ElevationImport elevation = new ElevationImport(Float.parseFloat(rs.getString("x")), Float.parseFloat(rs.getString("y")),
						Float.parseFloat(rs.getString("z")) * valueScale);
				return elevation;
			}
		});
		return results;
	}

	@Override
	public int getSrid() {
		return this.srid;
	}

	////////////////////////////////////////////////////////////////////////////////
	// Public (not overrided) methods
	////////////////////////////////////////////////////////////////////////////////

	public void executeSQL(String query) {
		LOGGER.debug("Exec: " + query);
		this.jdbcTemplate.execute(query);
	}

	public boolean testTableExistence(String tableName) {
		return this.jdbcTemplate.queryForObject("select count(tablename) = 1 from pg_tables where tablename = '" + tableName + "'", Boolean.class);
	}

	/**
	 * Find all points which intersect the including geometry and disjoint the excluding geometry (useful for multipolygon buildings with "hole").
	 * That method use a factor as argument in order to scale the including and excluding geometries.
	 * 
	 * TODO: use JTS instead of PostGis functions (ST_Scale and ST_Translate) to perform the scale/skrink process because this implementation is
	 * working but it takes 15 minutes to execute !
	 */
	public List<Coordinates> findAllPointsByGeometry(String includingGeomAsWKT, String excludingGeomAsWKT, float scaleFactor, int geomSrid) {
		final String geom = "ST_Transform(ST_GeomFromText('" + includingGeomAsWKT + "', " + geomSrid + "), " + this.srid + ")";
		String query = "SELECT x, y, z FROM " + this.tableName + ", " + geom + " as includingGeom";
		String condition = "ST_Scale(includingGeom, " + scaleFactor + ", " + scaleFactor + ")";
		condition = "ST_Translate(" + condition + ", " + "-" + scaleFactor
				+ "*(ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2 + ((ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2), " + "-" + scaleFactor
				+ "*(ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2 + ((ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2))";
		query += " WHERE ST_Intersects(geom, " + condition + ")";
		// TODO do the same for excluding geom (need cleanup before)
		// if (excludingGeomAsWKT != null) {
		// condition += " AND ST_Disjoint(geom, ST_Transform(ST_GeomFromText('" +
		// excludingGeomAsWKT + "', " + geomSrid + "), " + this.srid + "))";
		// }
		LOGGER.debug("Used query is: " + query);
		List<Coordinates> results = this.jdbcTemplate.query(query, new RowMapper<Coordinates>() {
			public Coordinates mapRow(ResultSet rs, int rowNum) throws SQLException {
				Coordinates coordinates = new Coordinates(rs.getString("x"), rs.getString("y"), rs.getString("z"));
				return coordinates;
			}
		});
		return results;
	}

}
