package org.openstreetmap.osmaxil.data.building;

import java.util.List;

import org.openstreetmap.osmaxil.data.AbstractElement;
import org.openstreetmap.osmaxil.data.ElementTagNames;
import org.openstreetmap.osmaxil.data.api.OsmApiTag;

public class BuildingElement extends AbstractElement {

    public BuildingElement(long osmId) {
        super(osmId);
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
    
    // Convenient methods

    public Float getHeight() {
        String s = (String) this.getTagValue(ElementTagNames.HEIGHT);
        return (s != null ? Float.parseFloat(s) : null);
    }

    public boolean setHeight(Float value) {
        return this.setTagValue(ElementTagNames.HEIGHT, value.toString());
    }

    public Integer getLevels() {
        String s = (String) this.getTagValue(ElementTagNames.BUILDING_LEVELS);
        return (s != null ? Integer.parseInt(s) : null);
    }

    public boolean setLevels(Integer value) {
        return this.setTagValue(ElementTagNames.BUILDING_LEVELS, value.toString());
    }

    public String getName() {
        return (String) this.getTagValue(ElementTagNames.NAME);
    }

    public boolean isPart() {
        return "yes".equals(this.getTagValue(ElementTagNames.BUILDING_PART));
    }

}
