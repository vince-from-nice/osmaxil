package org.openstreetmap.osmaxil.model.xml.pss;

import javax.xml.bind.annotation.XmlElement;

public class PssXmlBuilding {
    
    @XmlElement
    public String name;
    
    @XmlElement
    public String url;
    
    @XmlElement
    public String coordinates;
    
    @XmlElement
    public String height;
    
    @XmlElement
    public String estimatedHeight;
    
}
