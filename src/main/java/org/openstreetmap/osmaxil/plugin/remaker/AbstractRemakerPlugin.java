package org.openstreetmap.osmaxil.plugin.remaker;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiRoot;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;

public abstract class AbstractRemakerPlugin <ELEMENT extends AbstractElement, IMPORT extends AbstractImport>  
    extends AbstractPlugin<ELEMENT, IMPORT>  {
    
    protected Map<Long, OsmApiRoot> xmlForRemakingByElement;
    
    abstract public void buildXmlForRemaking(ELEMENT element);
    
    public AbstractRemakerPlugin() {
        this.xmlForRemakingByElement = new HashMap<Long, OsmApiRoot>();
    }
    
    public OsmApiRoot getXmlForRemaking(float osmId) {
        return this.xmlForRemakingByElement.get(osmId);
    }

}
