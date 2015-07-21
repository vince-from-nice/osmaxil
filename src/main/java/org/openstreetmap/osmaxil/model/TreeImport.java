package org.openstreetmap.osmaxil.model;


public class TreeImport extends AbstractImport {
    
    private String type;
    
    private String subType;

    private String reference;
    
    private int height;
    
    @Override
    public String getValueByTagName(String tagName) {
        if (ElementTag.GENUS.equals(tagName)) {
            return this.type.toString();
        } else if (ElementTag.SPECIFIES.equals(tagName)) {
            return this.subType.toString();
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Tree with id=[" + this.id + "], coords=[" + this.latitude + ", " + this.longitude + "], genus=["
                + this.type + "], subType=[" + this.subType + "], height=[" + this.height + "], ref=[" + this.reference + "]";
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

}
