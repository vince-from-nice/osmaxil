package org.openstreetmap.osmaxil.plugin;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiRoot;

public abstract class AbstracRemakerPlugin <Element extends AbstractElement, Import extends AbstractImport>  
    extends AbstractPlugin<Element, Import>  {
    
    protected Map<Long, OsmApiRoot> xmlForRemakingByElement;
    
    abstract public void buildXmlForRemaking(Element element);
    
    public AbstracRemakerPlugin() {
        this.xmlForRemakingByElement = new HashMap<Long, OsmApiRoot>();
    }
    
    public OsmApiRoot getXmlForRemaking(float osmId) {
        return this.xmlForRemakingByElement.get(osmId);
    }

}
