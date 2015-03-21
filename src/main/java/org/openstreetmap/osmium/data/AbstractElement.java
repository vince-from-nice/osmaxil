package org.openstreetmap.osmium.data;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmium.data.api.OsmApiRoot;
import org.openstreetmap.osmium.data.api.OsmApiTag;

public abstract class AbstractElement {

    private long osmId;
    
    private long relationId;

    private OsmApiRoot apiData;

    private List<AbstractImport> matchingImports;

    private AbstractImport bestMatchingImport;

    abstract public List<OsmApiTag> getTags();
    
    abstract public void updateChangeset(long changesetId);

    abstract public boolean isUpdatable();
    
    public AbstractElement(long osmId) {
        this.osmId = osmId;
        this.matchingImports = new ArrayList<AbstractImport>();
    }

    @Override
    public String toString() {
        return "OSM building has id=[" + this.getOsmId() + "]";
    }
    
    public Object getTagValue(String key) {
        Object value = null;
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
