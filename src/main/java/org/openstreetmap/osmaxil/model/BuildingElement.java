package org.openstreetmap.osmaxil.model;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlTag;

public class BuildingElement extends AbstractElement {
    
    private int computedArea;
        
	private String innerGeometryString; // used by building which has a "hole" (multipolygon)
    
    public BuildingElement(long osmId) {
        super(osmId);
    }
    
	@Override
	public ElementType getType() {
		return ElementType.Way;
	}
	
    @Override
    public void updateChangeset(long changesetId) {
        this.getApiData().ways.get(0).changeset = changesetId;
    }

    @Override
    public List<OsmXmlTag> getTags() {
    	if (this.getApiData() == null || this.getApiData().ways == null || this.getApiData().ways.get(0) == null) {
    		return new ArrayList<>();
    	}
        return this.getApiData().ways.get(0).tags;
    }
    
    @Override
    public void setTags(List<OsmXmlTag> tags) {
        this.getApiData().ways.get(0).tags = tags;
    }

    @Override
    public String toString() {
        return "OSM building has id=[" + this.getOsmId() + "], levels=[" + this.getLevels() + "], height=["
                + this.getHeight() + "], name=[" + this.getName() + "], part=[" + this.isPart() + "]";
    }

    public boolean isPart() {
        return "yes".equals(this.getTagValue(ElementTag.BUILDING_PART));
    }
    
    public Float getHeight() {
        try {
            String s = (String) this.getTagValue(ElementTag.HEIGHT);
            return (s != null ? Float.parseFloat(s) : null);
        } catch (Exception e) {
            LOGGER.warn("Unable to get levels for building import " + this.getOsmId() + " (" + e.getMessage()+ ")");
            return null;
        }
    }

    public boolean setHeight(Float value) {
        return this.setTagValue(ElementTag.HEIGHT, value.toString());
    }

    public Integer getLevels() {
        try {
            String s = (String) this.getTagValue(ElementTag.BUILDING_LEVELS);
            return (s != null ? Integer.parseInt(s) : null);
        } catch (Exception e) {
            LOGGER.warn("Unable to get levels for building import " + this.getOsmId() + " (" + e.getMessage()+ ")");
            return null;
        }
    }

    public boolean setLevels(Integer value) {
        return this.setTagValue(ElementTag.BUILDING_LEVELS, value.toString());
    }

    public int getComputedArea() {
        return computedArea;
    }

    public void setComputedArea(int computedArea) {
        this.computedArea = computedArea;
    }

    public static List<Long> getOuterOrInnerMemberIds(long relationId, String membersString, boolean outer) {
    	List<Long> results = new ArrayList<>();
        membersString = membersString.substring(1, membersString.length() - 1);
        String[] members = membersString.split(","); 
        for (int i = 0; i < members.length; i++) {
            if ((outer?"outer":"inner").equals(members[i]) && members[i-1].startsWith("w")) {
            	results.add(Long.parseLong(members[i - 1].substring(1)));
            }
        }
        return results;
    }

	public String getInnerGeometryString() {
		return innerGeometryString;
	}

	public void setInnerGeometryString(String innerGeometryString) {
		this.innerGeometryString = innerGeometryString;
	}
    
}
