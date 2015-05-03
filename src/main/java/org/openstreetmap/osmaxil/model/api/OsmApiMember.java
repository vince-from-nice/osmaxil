package org.openstreetmap.osmaxil.model.api;

import javax.xml.bind.annotation.XmlAttribute;

public class OsmApiMember {
    
    @XmlAttribute
    public long ref;
    
    @XmlAttribute
    public String role;
    
    @XmlAttribute
    public String type;

}
