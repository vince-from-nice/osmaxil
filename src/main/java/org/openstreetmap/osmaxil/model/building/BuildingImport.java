package org.openstreetmap.osmaxil.model.building;

import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTagNames;

public class BuildingImport extends AbstractImport {

    protected Double lat;

    protected Double lon;

    protected Integer levels;

    protected Float height;
    
    protected String url;

    protected Integer area;

    // TODO
    protected String geometry;
    
    @Override
    public String getTagValue(String tagName) {
        if (ElementTagNames.BUILDING_LEVELS.equals(tagName)) {
            return this.levels.toString();
        } else if (ElementTagNames.HEIGHT.equals(tagName)) {
            return this.height.toString();
        }else if (ElementTagNames.URL.equals(tagName)) {
            return this.url.toString();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Building import with id=[" + this.id + "] and name=[" + this.name + "], lat=[" + this.lat + "], lon=["
                + this.lon + "], levels=[" + this.levels + "], height=[" + this.height + "], area=[" + this.area + "], url=[" + this.url + "]";
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
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

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
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

}