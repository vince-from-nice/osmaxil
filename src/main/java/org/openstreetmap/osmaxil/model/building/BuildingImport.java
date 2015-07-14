package org.openstreetmap.osmaxil.model.building;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTagNames;
import org.openstreetmap.osmaxil.model.StringCoordinates;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class BuildingImport extends AbstractImport {

    protected Integer levels;

    protected Float height;

    protected String url;

    protected Integer area;

    protected String geometryRawString; // useful for debug only

    private String geometryAsWKT; // useful for PostGIS queries

    protected Polygon geometryAsPolygon; // useless

    protected List<Point> points = new ArrayList<>(); // useless

    protected List<StringCoordinates> coordinates = new ArrayList<>(); // keep coordinates as strings (no more rounding issues)

    @Override
    public String getValueByTagName(String tagName) {
        if (ElementTagNames.BUILDING_LEVELS.equals(tagName)) {
            return this.levels.toString();
        } else if (ElementTagNames.HEIGHT.equals(tagName)) {
            return this.height.toString();
        } else if (ElementTagNames.URL.equals(tagName)) {
            return this.url;
        } else if (ElementTagNames.NAME.equals(tagName)) {
            return this.name;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Building import with id=[" + this.id + "] and name=[" + this.name + "], lat=[" + this.latitude + "], lon=["
                + this.longitude + "], levels=[" + this.levels + "], height=[" + this.height + "], area=[" + this.area
                + "], url=[" + this.url + "]";
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

    public Polygon getPolygon() {
        return geometryAsPolygon;
    }

    public void setPolygon(Polygon polygon) {
        this.geometryAsPolygon = polygon;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public List<StringCoordinates> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<StringCoordinates> coordinates) {
        this.coordinates = coordinates;
    }

}