package org.openstreetmap.osmaxil.model.api;

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
    public List<OsmApiNode> nodes;
    
    @XmlElement (name="way")
    public List<OsmApiWay> ways;
    
    @XmlElement (name="relations")
    public List<OsmApiRelation> relations;

}
