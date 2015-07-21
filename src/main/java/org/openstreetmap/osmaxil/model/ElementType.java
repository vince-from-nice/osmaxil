package org.openstreetmap.osmaxil.model;

public enum ElementType {   
    
    Node("node"),
    
    Way("way"),
    
    Relation("relation");
    
    private String name;
    
    private ElementType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
