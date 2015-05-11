package org.openstreetmap.osmaxil.model.xml.pss;

import javax.xml.bind.annotation.XmlElement;

public class PssBuilding {
    
    @XmlElement
    public String url;
    
    @XmlElement
    public String coordinates;
    
    @XmlElement
    public String height;
    
    @XmlElement
    public String estimatedHeight;
    
}
