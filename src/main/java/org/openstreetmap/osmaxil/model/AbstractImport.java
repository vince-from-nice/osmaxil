package org.openstreetmap.osmaxil.model;

import org.openstreetmap.osmaxil.model.misc.MatchableObject;

public abstract class AbstractImport extends MatchableObject {

    protected long id;
    
    protected String name;
    
    protected Double latitude;

    protected Double longitude;
    
    protected AbstractElement matchingElement;
    
    abstract public String getValueByTagName(String tagName);
    
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
    
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public AbstractElement getMatchingElement() {
        return matchingElement;
    }

    public void setMatchingElement(AbstractElement matchingElement) {
        this.matchingElement = matchingElement;
    }
    
}
