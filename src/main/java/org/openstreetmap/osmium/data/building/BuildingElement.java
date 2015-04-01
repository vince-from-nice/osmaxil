package org.openstreetmap.osmium.data.building;

import java.util.List;

import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.api.OsmApiTag;

public class BuildingElement extends AbstractElement {

    private Float originalHeight;
    
    private Integer originalLevels;
    
    private boolean updated = false;
    
    public BuildingElement(long osmId) {
        super(osmId);
        this.originalHeight = null;
        this.originalLevels = null;
        this.updated = false;
    }
    
    // Overrided methods
    
    @Override
    public boolean isUpdated() {
        return this.updated;
    }
    
    @Override
    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
    
    @Override
    public void updateChangeset(long changesetId) {
        this.getApiData().ways.get(0).changeset = changesetId;
    }

    @Override
    public List<OsmApiTag> getTags() {
        return this.getApiData().ways.get(0).tags;
    }

//    @Override
//    public boolean isVirgin(List<String> updatableTagNames) {
//        //return this.getHeight() == null || this.getLevels() == null;
//        for (String updatableTagName : updatableTagNames) {
//            if (this.getTagValue(updatableTagName) != null) {
//                return false;
//            }
//        }
//        return true;
//    }

    @Override
    public String toString() {
        return "OSM building has id=[" + this.getOsmId() + "], levels=[" + this.getLevels() + "], height=["
                + this.getHeight() + "], name=[" + this.getName() + "], part=[" + this.isPart() + "]";
    }
    
    // Convenient methods

    public Float getHeight() {
        String s = (String) this.getTagValue("height");
        return (s != null ? Float.parseFloat(s) : null);
    }

    public boolean setHeight(Float value) {
        return this.setTagValue("height", value.toString());
    }

    public Integer getLevels() {
        String s = (String) this.getTagValue("building:levels");
        return (s != null ? Integer.parseInt(s) : null);
    }

    public boolean setLevels(Integer value) {
        return this.setTagValue("building:levels", value.toString());
    }

    public String getName() {
        return (String) this.getTagValue("name");
    }

    public boolean isPart() {
        return "yes".equals(this.getTagValue("building:part"));
    }

    public Float getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(Float originalHeight) {
        this.originalHeight = originalHeight;
    }

    public Integer getOriginalLevels() {
        return originalLevels;
    }

    public void setOriginalLevels(Integer originalLevels) {
        this.originalLevels = originalLevels;
    }

}
