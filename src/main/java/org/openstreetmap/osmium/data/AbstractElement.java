package org.openstreetmap.osmium.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmium.data.api.OsmApiRoot;
import org.openstreetmap.osmium.data.api.OsmApiTag;

public abstract class AbstractElement {

    private long osmId;
    
    private long relationId;
    
    private OsmApiRoot apiData;

    private List<AbstractImport> matchingImports;

    private AbstractImport bestMatchingImport;
    
    private Map<String, Map<String, List<AbstractImport>>> importsByTagValueByTagNames;
    
    private Map<String, Map<String, Float>> totalScoresByTagValueByTagNames;
    
    abstract public List<String> getUpdatableTagNames();

    abstract public List<OsmApiTag> getTags();
    
    abstract public void updateChangeset(long changesetId);

    abstract public boolean isUpdatable();
    
    abstract public boolean isUpdated();
    
    abstract public void setUpdated(boolean updated);
    
    public AbstractElement(long osmId) {
        this.osmId = osmId;
        this.matchingImports = new ArrayList<AbstractImport>();
        this.importsByTagValueByTagNames = new HashMap<String, Map<String,List<AbstractImport>>>();
        this.totalScoresByTagValueByTagNames = new HashMap<String, Map<String,Float>>();
    }

    @Override
    public String toString() {
        return "OSM building has id=[" + this.getOsmId() + "]";
    }
    
    public String getTagValue(String key) {
        String value = null;
        for (OsmApiTag tag : getTags()) {
            if (tag.k.equals(key)) {
                value = tag.v;
            }
        }
        return value;
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

    public Map<String, List<AbstractImport>> getMatchingImportsByTagValueByTagName(String tagName) {
        Map<String, List<AbstractImport>> result = this.importsByTagValueByTagNames.get(tagName);
        if (result == null) {
            result = new HashMap<String, List<AbstractImport>>();
            this.importsByTagValueByTagNames.put(tagName, result);
        }
        return result;
    }
    
    public Map<String, Float> getTotalScoresByTagValueByTagName(String tagName) {
        Map<String, Float> result = this.totalScoresByTagValueByTagNames.get(tagName);
        if (result == null) {
            result = new HashMap<String, Float>();
            this.totalScoresByTagValueByTagNames.put(tagName, result);
        }
        return result;
    }
    
    // Getters & Setters

    public long getOsmId() {
        return osmId;
    }

    public void setOsmId(long osmId) {
        this.osmId = osmId;
    }

    public OsmApiRoot getApiData() {
        return apiData;
    }

    public void setApiData(OsmApiRoot apiData) {
        this.apiData = apiData;
    }

    public List<AbstractImport> getMatchingImports() {
        return matchingImports;
    }

    public void setMatchingImports(List<AbstractImport> matchingImports) {
        this.matchingImports = matchingImports;
    }

    public AbstractImport getBestMatchingImport() {
        return bestMatchingImport;
    }

    public void setBestMatchingImport(AbstractImport bestMatchingImport) {
        this.bestMatchingImport = bestMatchingImport;
    }

    public long getRelationId() {
        return relationId;
    }

    public void setRelationId(long relationId) {
        this.relationId = relationId;
    }

}
