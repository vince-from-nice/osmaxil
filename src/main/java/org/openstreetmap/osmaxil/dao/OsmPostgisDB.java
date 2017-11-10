package org.openstreetmap.osmaxil.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.BuildingElement;
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
		// TODO
		return null;
	}

	public Long[] findElementIdsByQuery(String query) {
		List<Long> result = this.jdbcTemplate.query(query, new RowMapper<Long>() {
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return Long.parseLong(rs.getString("osm_id"));
			}
		});
		return result.toArray(new Long[result.size()]);
	}

	public class IdWithDouble {
		public long id;
		public double d;
	}

	public IdWithDouble[] findElementIdsWithDoubleByQuery(String query) {
		List<IdWithDouble> result = this.jdbcTemplate.query(query, new RowMapper<IdWithDouble>() {
			public IdWithDouble mapRow(ResultSet rs, int rowNum) throws SQLException {
				IdWithDouble idWithDouble = new IdWithDouble();
				idWithDouble.id = Long.parseLong(rs.getString("osm_id"));
				idWithDouble.d = Double.parseDouble(rs.getString("score"));
				return idWithDouble;
			}
		});
		return result.toArray(new IdWithDouble[result.size()]);
	}

	public class IdWithString {
		public long id;
		public String string;
	}

	public IdWithString[] findElementIdsWithGeomByQuery(String query) {
		List<IdWithString> result = this.jdbcTemplate.query(query, new RowMapper<IdWithString>() {
			public IdWithString mapRow(ResultSet rs, int rowNum) throws SQLException {
				IdWithString idWithGeom = new IdWithString();
				idWithGeom.id = Long.parseLong(rs.getString("osm_id"));
				idWithGeom.string = rs.getString("geomAsWKT");
				return idWithGeom;
			}
		});
		return result.toArray(new IdWithString[result.size()]);
	}

	public String getRelationMembers(long relationId) {
		String result = "";
		String query = "select members from planet_osm_rels where id= ? ;";
		result = this.jdbcTemplate.queryForObject(query, String.class, relationId);
		return result;
	}

	public int getPolygonArea(long osmId) {
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
		List<Coordinates> result = this.jdbcTemplate.query(query, new RowMapper<Coordinates>() {
			public Coordinates mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new Coordinates(rs.getString("x"), rs.getString("y"), "0");
			}
		}, osmId);
		// TODO handle multi outer members
		return result.get(0);
	}

	public int getSrid() {
		return srid;
	}

	public List<BuildingElement> findBuildingsByArea(String includingAreaString, String excludingAreaString, int filteringAreaSrid) {
		// Use a map because some building could be fetched twice from PostGIS (ie. outer members of building relations which has is own tags)
		Map<Long, BuildingElement> buildings = new HashMap<>();
		List<Long> targetedRelationIds = new ArrayList<>();
		// Get only elements whose coordinates are fine with the filtering areas
		String condition = "ST_Intersects(way, ST_Transform(ST_GeomFromText('" + includingAreaString + "', " + filteringAreaSrid + "), " + this.srid
				+ "))";
		condition += " AND ST_Disjoint(way, ST_Transform(ST_GeomFromText('" + excludingAreaString + "', " + filteringAreaSrid + "), " + this.srid
				+ "))";
		String query = "SELECT osm_id, ST_AsText(way) AS geomAsWKT, 1 FROM planet_osm_polygon WHERE building <> '' AND " + condition;
		// Specify building IDs directly (used for debugging)
		// query = "SELECT osm_id, ST_AsText(way) AS geomAsWKT, 1 FROM planet_osm_polygon WHERE osm_id = -6640171";
		LOGGER.debug("Used query is: " + query);
		// Fetch from DB the IDs and the geometries
		OsmPostgisDB.IdWithString[] idsWithGeom = this.findElementIdsWithGeomByQuery(query);
		for (OsmPostgisDB.IdWithString idWithGeom : idsWithGeom) {
			// If ID is negative it means the element is normal (ie. a way)
			if (idWithGeom.id > 0) {
				BuildingElement building = new BuildingElement(idWithGeom.id);
				building.setGeometryString(idWithGeom.string);
				buildings.put(building.getOsmId(), building);
			}
			// If ID is negative it means the element is a relation
			// See http://wiki.openstreetmap.org/wiki/Osm2pgsql/schema for details
			else {
				long relationId = -idWithGeom.id;
				if (targetedRelationIds.contains(relationId)) {
					LOGGER.info("Relation with ID=" + relationId + " has already been targeted, skipping it...");
				} else {
					targetedRelationIds.add(relationId);
					String membersString = this.getRelationMembers(relationId);
					List<Long> outerMemberIds = BuildingElement.getOuterOrInnerMemberIds(relationId, membersString, true);
					// For now only relations with only one "outer" member are supported
					// See my thread on the french OSM forum:
					// http://forum.openstreetmap.fr/viewtopic.php?f=5&t=6397&sid=2986b3c59cfc7c1877237b8ad8982110
					/*
					 * long outerMemberId = outerMemberIds.get(currentOuterMemberIndex); this.outerMemberIndexes.put(relationId,
					 * ++currentOuterMemberIndex); LOGGER.info("Outer member ID selected is " + outerMemberId + " (current index is " +
					 * currentOuterMemberIndex + "), creating a new element with it");
					 */
					if (outerMemberIds.size() > 1) {
						LOGGER.warn("Relation with ID=" + relationId
								+ " is new but it has several outer members, only relation with an unique outer member are supported for now.");
					} else if (outerMemberIds.size() < 1) {
						LOGGER.warn("Relation with ID=" + relationId
								+ " is new but it doesn't have any outer members, only relation with an unique outer member are supported for now.");
					} else {
						// Create a new element from the unique outer member
						long outerMemberId = outerMemberIds.get(0);
						LOGGER.info("Relation with ID=" + relationId + " is new, create a new targeted element from its unique outer member (id="
								+ outerMemberId + ")");
						BuildingElement building = new BuildingElement(outerMemberId);
						building.setRelationId(relationId);
						building.setGeometryString(idWithGeom.string);
						buildings.put(building.getOsmId(), building);
					}
				}
			}
		}
		LOGGER.info("Number of returned buildings: " + buildings.size());
		return new ArrayList<>(buildings.values());
	}

}
