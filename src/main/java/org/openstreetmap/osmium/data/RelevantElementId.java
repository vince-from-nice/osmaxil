package org.openstreetmap.osmium.data;

public class RelevantElementId {
    
    private long osmId;
    
    private long relationId;

    public long getOsmId() {
        return osmId;
    }

    public void setOsmId(long osmId) {
        this.osmId = osmId;
    }

    public long getRelationId() {
        return relationId;
    }

    public void setRelationId(long relationId) {
        this.relationId = relationId;
    }
    

}
