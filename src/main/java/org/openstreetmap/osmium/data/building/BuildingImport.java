package org.openstreetmap.osmium.data.building;

import org.openstreetmap.osmium.data.AbstractImport;
import org.openstreetmap.osmium.data.ElementTagNames;

public class BuildingImport extends AbstractImport {

    protected Double lat;

    protected Double lon;

    protected Integer levels;

    protected Float height;

    protected Integer area;

    // TODO
    protected String geometry;
    
    @Override
    public String getTagValue(String tagName) {
        if (ElementTagNames.BUILDING_LEVELS.equals(tagName)) {
            if (this.levels != null){
                return this.levels.toString();
            }
        } else if (ElementTagNames.HEIGHT.equals(tagName)) {
            if (this.height != null) {
                return this.height.toString();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Building import with id=[" + this.id + "] and name=[" + this.name + "], lat=[" + this.lat + "], lon=["
                + this.lon + "], levels=[" + this.levels + "], height=[" + this.height + "], area=[" + this.area + "]";
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

}