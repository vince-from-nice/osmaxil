package org.openstreetmap.osmium.data;

public abstract class AbstractImport {

    protected long id;
    
    protected String name;
    
    protected AbstractElement element;
    
    protected float matchingScore;
    
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
    
}
