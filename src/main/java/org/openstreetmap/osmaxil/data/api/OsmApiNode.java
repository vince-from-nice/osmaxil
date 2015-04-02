package org.openstreetmap.osmaxil.data.api;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class OsmApiNode {
    
    @XmlAttribute
    public Long id;
    
    @XmlAttribute
    public int version;
    
    @XmlAttribute
    public long changeset;
    
    @XmlAttribute
    public String user;
    
    @XmlAttribute
    public String visible;
    
    @XmlAttribute
    public int uid;
    
    @XmlAttribute
    public String timestamp;
    
    // Specific attributes
    
    @XmlAttribute
    public float lat;
    
    @XmlAttribute
    public float lon;
    
    @XmlElement (name="tag")
    public List<OsmApiTag> tags;

}
