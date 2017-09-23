package org.openstreetmap.osmaxil.dao.xml.osm;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name="osm")
public class OsmXmlRoot {
    
    @XmlAttribute
    public float version;
    
    @XmlAttribute
    public String generator;
    
    @XmlElement (name="node")
    public List<OsmXmlNode> nodes = new ArrayList<>();;
    
    @XmlElement (name="way")
    public List<OsmXmlWay> ways = new ArrayList<>();;
    
    @XmlElement (name="relations")
    public List<OsmXmlRelation> relations = new ArrayList<>();

}
