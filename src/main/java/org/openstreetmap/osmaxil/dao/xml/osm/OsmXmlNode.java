package org.openstreetmap.osmaxil.dao.xml.osm;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class OsmXmlNode {
    
    @XmlAttribute
    public Long id;
    
    @XmlAttribute
    public int version;
    
    @XmlAttribute
    public Long changeset;
    
    @XmlAttribute
    public String user;
    
    @XmlAttribute
    public String visible;
    
    @XmlAttribute
    public Integer uid;
    
    @XmlAttribute
    public String timestamp;
    
    @XmlAttribute
    public String action;
    
    // Specific attributes
    
    @XmlAttribute
    public String lat;
    
    @XmlAttribute
    public String lon;
    
    @XmlElement (name="tag")
    public List<OsmXmlTag> tags = new ArrayList<>();;

}
