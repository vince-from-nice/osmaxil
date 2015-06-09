package org.openstreetmap.osmaxil.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiRoot;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiTag;

public abstract class AbstractElement {

    protected Long osmId;

    protected OsmApiRoot apiData;

    private long relationId;
    
    private boolean altered;
    
    private float matchingScore;
    
    abstract public List<OsmApiTag> getTags();
    
    private List<AbstractImport> matchingImports;
    
    private Map<String, String> originalValuesByTagNames;

    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    abstract public void updateChangeset(long changesetId);

    public AbstractElement(long osmId) {
        this.osmId = osmId;
        this.altered = false;
        this.matchingImports = new ArrayList<AbstractImport>();
        this.originalValuesByTagNames = new HashMap<String, String>();
    }
    
    public List<AbstractImport> getMatchingImports() {
        return this.matchingImports;
    }
    
    public String getTagValue(String key) {
        List<OsmApiTag> tags = this.getTags();
        if (tags == null) {
            //LOGGER.warn("Unable to get tag value of " + key + " for element " + this.getOsmId() + " because its tag list is null !!");
            return null;
        }
        for (OsmApiTag tag : tags) {
            if (tag.k.equals(key)) {
                return tag.v;
            }
        }
        return null;
    }

    public boolean setTagValue(String key, String value) {
        for (OsmApiTag tag : this.getTags()) {
            if (tag.k.equals(key)) {
                tag.v = value;
                return true;
            }
        }
        OsmApiTag tag = new OsmApiTag();
        tag.k = key;
        tag.v = value;
        this.getTags().add(tag);
        return false;
    }

    @Override
    public String toString() {
        return "OSM building has id=[" + this.getOsmId() + "]";
    }

    public String getName() {
        return (String) this.getTagValue(ElementTagNames.NAME);
    }
    
    // Getters & Setters

    public Long getOsmId() {
        return osmId;
    }

    public void setOsmId(Long osmId) {
        this.osmId = osmId;
    }

    public OsmApiRoot getApiData() {
        return apiData;
    }

    public void setApiData(OsmApiRoot apiData) {
        this.apiData = apiData;
    }
    
    public boolean isAltered() {
        return this.altered;
    }
    
    public void setAltered(boolean altered) {
        this.altered = altered;
    }

    public long getRelationId() {
        return relationId;
    }

    public void setRelationId(long relationId) {
        this.relationId = relationId;
    }

    public float getMatchingScore() {
        return matchingScore;
    }

    public void setMatchingScore(float matchingScore) {
        this.matchingScore = matchingScore;
    }

    public Map<String, String> getOriginalValuesByTagNames() {
        return originalValuesByTagNames;
    }

    public void setOriginalValuesByTagNames(Map<String, String> originalValuesByTagNames) {
        this.originalValuesByTagNames = originalValuesByTagNames;
    }

}
