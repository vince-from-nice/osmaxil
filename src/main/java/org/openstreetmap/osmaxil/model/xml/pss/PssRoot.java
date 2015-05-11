package org.openstreetmap.osmaxil.model.xml.pss;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name="buildings")
public class PssRoot {

    @XmlElement (name="building")
    public List<PssBuilding> buildings;
    
}
