package org.openstreetmap.osmaxil.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.annotation.Obsolete;
import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;
import org.openstreetmap.osmaxil.model.api.OsmApiTag;

public abstract class AbstractElement {

    protected long osmId;

    protected OsmApiRoot apiData;
    
    abstract public List<OsmApiTag> getTags();
    
    private long relationId;
        
    private boolean updated;
    
    private boolean remaked;

    private List<AbstractImport> matchingImports;

    // Used by remaker plugins
    
    private OsmApiRoot remakingData;

    // Used by updater plugins
    
    private Map<String, Map<String, List<AbstractImport>>> importsByTagValuesByTagNames;
    
    private Map<String, Map<String, Float>> totalScoresByTagValuesByTagNames;
    
    private Map<String, String> originalValuesByTagNames;

    @Obsolete
    private AbstractImport bestMatchingImport;
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    abstract public void updateChangeset(long changesetId);

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
    
    public AbstractElement(long osmId) {
        this.osmId = osmId;
        this.updated = false;
        this.matchingImports = new ArrayList<AbstractImport>();
        this.importsByTagValuesByTagNames = new HashMap<String, Map<String,List<AbstractImport>>>();
        this.totalScoresByTagValuesByTagNames = new HashMap<String, Map<String,Float>>();
        this.originalValuesByTagNames = new HashMap<String, String>();
    }

    public Map<String, List<AbstractImport>> getMatchingImportsByTagValuesByTagName(String tagName) {
        Map<String, List<AbstractImport>> result = this.importsByTagValuesByTagNames.get(tagName);
        if (result == null) {
            result = new HashMap<String, List<AbstractImport>>();
            this.importsByTagValuesByTagNames.put(tagName, result);
        }
        return result;
    }
    
    public Map<String, Float> getTotalScoresByTagValuesByTagName(String tagName) {
        Map<String, Float> result = this.totalScoresByTagValuesByTagNames.get(tagName);
        if (result == null) {
            result = new HashMap<String, Float>();
            this.totalScoresByTagValuesByTagNames.put(tagName, result);
        }
        return result;
    }
    
    public Float getBestTotalScoreByTagName(String tagName) {
        Float bestTotalScore = null;
        for (Float totalScore : this.getTotalScoresByTagValuesByTagName(tagName).values()) {
            if (bestTotalScore == null || bestTotalScore < totalScore) {
                bestTotalScore = totalScore;
            }
        }
        return bestTotalScore;
    }
    
    public String getBestTagValueByTagName(String tagName) {
        String bestTagValue = null;
        Float bestTotalScore = null;
        Map<String, List<AbstractImport>> map = this.getMatchingImportsByTagValuesByTagName(tagName);
        for (String tagValue : map.keySet()) {
            List<AbstractImport> importList = map.get(tagValue);
            // TODO use precalculated total scores
            float totalScore = 0;
            for (AbstractImport imp : importList) {
                totalScore += imp.getMatchingScore();
            }
            if (bestTotalScore == null || bestTotalScore < totalScore) {
                bestTotalScore = totalScore;
                bestTagValue = tagValue;
            }
        }
        return bestTagValue;
    }
    
    @Override
    public String toString() {
        return "OSM building has id=[" + this.getOsmId() + "]";
    }

    public String getName() {
        return (String) this.getTagValue(ElementTagNames.NAME);
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
    
    public boolean isUpdated() {
        return this.updated;
    }
    
    public void setUpdated(boolean updated) {
        this.updated = updated;
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

    public Map<String, String> getOriginalValuesByTagNames() {
        return originalValuesByTagNames;
    }

    public void setOriginalValuesByTagNames(Map<String, String> orignalValuesByTagNames) {
        this.originalValuesByTagNames = orignalValuesByTagNames;
    }

    public boolean isRemaked() {
        return remaked;
    }

    public void setRemaked(boolean remaked) {
        this.remaked = remaked;
    }

    public OsmApiRoot getRemakingData() {
        return remakingData;
    }

    public void setRemakingData(OsmApiRoot remakingData) {
        this.remakingData = remakingData;
    }

}
