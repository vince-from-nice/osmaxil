package org.openstreetmap.osmaxil.model;


public class NaturalTreeImport extends AbstractImport {
    
    private String genus;
    
    private String species;

    private String reference;
    
    private Integer height;
    
    @Override
    public String getValueByTagName(String tagName) {
        if (ElementTag.GENUS.equals(tagName)) {
            return this.genus.toString();
        } else if (ElementTag.SPECIFIES.equals(tagName)) {
            return this.species.toString();
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Tree with id=[" + this.id + "], coords=[" + this.latitude + ", " + this.longitude + "], genus=["
                + this.genus + "], species=[" + this.species + "], height=[" + this.height + "], ref=[" + this.reference + "]";
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String subType) {
        this.species = subType;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

}
