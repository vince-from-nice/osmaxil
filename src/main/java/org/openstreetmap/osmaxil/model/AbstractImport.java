package org.openstreetmap.osmaxil.model;

public abstract class AbstractImport {

    protected long id;
    
    protected String name;
    
    protected AbstractElement element;
    
    protected float matchingScore;
    
    protected Double lat;

    protected Double lon;
    
    abstract public String getTagValue(String tagName);
    
    @Override
    public String toString() {
        return "Element with id=[" + this.id + "] and name=[" + this.name + "]";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractElement getElement() {
        return element;
    }

    public void setElement(AbstractElement element) {
        this.element = element;
    }

    public float getMatchingScore() {
        return matchingScore;
    }

    public void setMatchingScore(float matchingScore) {
        this.matchingScore = matchingScore;
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
    
}
