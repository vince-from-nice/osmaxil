package org.openstreetmap.osmium.data;

public class BuildingImport extends AbstractImport {

    Double lat;

    Double lon;

    Integer levels;

    Integer height;

    Integer area;

    // TODO
    String geometry;

    @Override
    public String toString() {
        return "Building with id=[" + this.id + "] and name=[" + this.name + "], lat=[" + this.lat + "], lon=["
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

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
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