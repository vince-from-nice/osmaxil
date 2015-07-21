package org.openstreetmap.osmaxil.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.misc.MatchableObject;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlTag;

public abstract class AbstractElement extends MatchableObject {

    protected Long osmId;

    protected OsmXmlRoot apiData;

    private long relationId;
    
    private boolean altered;
    
    // TODO move these attributes into plugins ?
    private List<AbstractImport> matchingImports;
    private Map<String, String> originalValuesByTagNames;

    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    abstract public void updateChangeset(long changesetId);

    abstract public List<OsmXmlTag> getTags();
    
    abstract public void setTags(List<OsmXmlTag> tags);
    
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
        List<OsmXmlTag> tags = this.getTags();
        if (tags == null) {
            //LOGGER.warn("Unable to get tag value of " + key + " for element " + this.getOsmId() + " because its tag list is null !!");
            return null;
        }
        for (OsmXmlTag tag : tags) {
            if (tag.k.equals(key)) {
                return tag.v;
            }
        }
        return null;
    }

    public boolean setTagValue(String key, String value) {
        for (OsmXmlTag tag : this.getTags()) {
            if (tag.k.equals(key)) {
                tag.v = value;
                return true;
            }
        }
        OsmXmlTag tag = new OsmXmlTag();
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
        return (String) this.getTagValue(ElementTag.NAME);
    }
    
    // Getters & Setters

    public Long getOsmId() {
        return osmId;
    }

    public void setOsmId(Long osmId) {
        this.osmId = osmId;
    }

    public OsmXmlRoot getApiData() {
        return apiData;
    }

    public void setApiData(OsmXmlRoot apiData) {
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

    public Map<String, String> getOriginalValuesByTagNames() {
        return originalValuesByTagNames;
    }

    public void setOriginalValuesByTagNames(Map<String, String> originalValuesByTagNames) {
        this.originalValuesByTagNames = originalValuesByTagNames;
    }

}
