package org.openstreetmap.osmaxil.model.xml.osm;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class OsmApiRelation {
    
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
    
    @XmlElement (name="tag")
    public List<OsmApiTag> tags;
    
    @XmlElement (name="members")
    public List<OsmApiMember> members;
    
}
