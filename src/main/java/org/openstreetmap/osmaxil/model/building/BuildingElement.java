package org.openstreetmap.osmaxil.model.building;

import java.util.List;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.misc.ElementTagNames;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlTag;

public class BuildingElement extends AbstractElement {
    
    private int computedArea;
    
    public BuildingElement(long osmId) {
        super(osmId);
    }
    
    @Override
    public void updateChangeset(long changesetId) {
        this.getApiData().ways.get(0).changeset = changesetId;
    }

    @Override
    public List<OsmXmlTag> getTags() {
        return this.getApiData().ways.get(0).tags;
    }

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
