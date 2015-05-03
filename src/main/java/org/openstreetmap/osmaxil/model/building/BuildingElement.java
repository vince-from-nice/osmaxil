package org.openstreetmap.osmaxil.model.building;

import java.util.List;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTagNames;
import org.openstreetmap.osmaxil.model.api.OsmApiTag;

public class BuildingElement extends AbstractElement {
    
    private int computedArea;
    
    public BuildingElement(long osmId) {
        super(osmId);
        this.computedArea = 0;
    }
    
    // Overrided methods
        
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

    public boolean isPart() {
        return "yes".equals(this.getTagValue(ElementTagNames.BUILDING_PART));
    }
    
    public Float getHeight() {
        try {
            String s = (String) this.getTagValue(ElementTagNames.HEIGHT);
            return (s != null ? Float.parseFloat(s) : null);
        } catch (Exception e) {
            LOGGER.warn("Unable to get levels for building import " + this.getOsmId() + " (" + e.getMessage()+ ")");
            return null;
        }
    }

    public boolean setHeight(Float value) {
        return this.setTagValue(ElementTagNames.HEIGHT, value.toString());
    }

    public Integer getLevels() {
        try {
            String s = (String) this.getTagValue(ElementTagNames.BUILDING_LEVELS);
            return (s != null ? Integer.parseInt(s) : null);
        } catch (Exception e) {
            LOGGER.warn("Unable to get levels for building import " + this.getOsmId() + " (" + e.getMessage()+ ")");
            return null;
        }
    }

    public boolean setLevels(Integer value) {
        return this.setTagValue(ElementTagNames.BUILDING_LEVELS, value.toString());
    }

    public int getComputedArea() {
        return computedArea;
    }

    public void setComputedArea(int computedArea) {
        this.computedArea = computedArea;
    }

}
