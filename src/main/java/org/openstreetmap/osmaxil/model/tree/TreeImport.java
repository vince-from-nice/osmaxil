package org.openstreetmap.osmaxil.model.tree;

import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.misc.ElementTagNames;

public class TreeImport extends AbstractImport {
    
    private String type;
    
    private String subType;

    private String referenceCode;
    
    private int height;
    
    @Override
    public String getValueByTagName(String tagName) {
        if (ElementTagNames.GENUS.equals(tagName)) {
            return this.type.toString();
        } else if (ElementTagNames.SPECIFIES.equals(tagName)) {
            return this.subType.toString();
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Tree with id=[" + this.id + "], coords=[" + this.latitude + ", " + this.longitude + "], genus=["
                + this.type + "], subType=[" + this.subType + "], height=[" + this.height + "]";
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
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
