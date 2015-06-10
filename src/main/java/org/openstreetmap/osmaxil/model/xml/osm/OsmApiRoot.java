package org.openstreetmap.osmaxil.model.xml.osm;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name="osm")
public class OsmApiRoot {
    
    @XmlAttribute
    public float version;
    
    @XmlAttribute
    public String generator;
    
    @XmlElement (name="node")
    public List<OsmApiNode> nodes = new ArrayList<>();;
    
    @XmlElement (name="way")
    public List<OsmApiWay> ways = new ArrayList<>();;
    
    @XmlElement (name="relations")
    public List<OsmApiRelation> relations = new ArrayList<>();

}
