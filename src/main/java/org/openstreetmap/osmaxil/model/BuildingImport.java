package org.openstreetmap.osmaxil.model;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.misc.Coordinates;

import com.vividsolutions.jts.geom.Point;

public class BuildingImport extends AbstractImport {

	protected Integer levels;

	protected Float height;

	protected String url;

	protected Integer area;

	private String geometryAsWKT;

	protected String geometryRawString;

	protected List<Point> points = new ArrayList<>();

	protected List<Coordinates> coordinates = new ArrayList<>(); // keep coordinates as strings (no more rounding issues)

	@Override
	public String getValueByTagName(String tagName) {
		if (ElementTag.BUILDING_LEVELS.equals(tagName)) {
			return this.levels.toString();
		} else if (ElementTag.HEIGHT.equals(tagName)) {
			return this.height.toString();
		} else if (ElementTag.URL.equals(tagName)) {
			return this.url;
		} else if (ElementTag.NAME.equals(tagName)) {
			return this.name;
		}
		return null;
	}

	@Override
	public String toString() {
		return "Building import with id=[" + this.id + "] and name=[" + this.name + "], lat=[" + this.latitude + "], lon=[" + this.longitude
				+ "], levels=[" + this.levels + "], height=[" + this.height + "], area=[" + this.area + "], url=[" + this.url + "]";
	}

	public Integer getLevels() {
		return levels;
	}

	public void setLevels(Integer levels) {
		this.levels = levels;
	}

	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

	public Integer getArea() {
		return area;
	}

	public void setArea(Integer area) {
		this.area = area;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getGeometryRawString() {
		return geometryRawString;
	}

	public void setGeometryRawString(String geometryString) {
		this.geometryRawString = geometryString;
	}

	public String getGeometryAsWKT() {
		return geometryAsWKT;
	}

	public void setGeometryAsWKT(String geometryWKT) {
		this.geometryAsWKT = geometryWKT;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}

	public List<Coordinates> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<Coordinates> coordinates) {
		this.coordinates = coordinates;
	}

}