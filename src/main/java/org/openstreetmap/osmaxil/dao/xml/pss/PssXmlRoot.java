package org.openstreetmap.osmaxil.dao.xml.pss;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name="buildings")
public class PssXmlRoot {

    @XmlElement (name="building")
    public List<PssXmlBuilding> buildings;
    
}
