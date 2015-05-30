package org.openstreetmap.osmaxil.plugin.remaker;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiRoot;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;

public abstract class AbstractRemakerPlugin <Element extends AbstractElement, Import extends AbstractImport>  
    extends AbstractPlugin<Element, Import>  {
    
    protected Map<Long, OsmApiRoot> xmlForRemakingByElement;
    
    abstract public void buildXmlForRemaking(Element element);
    
    public AbstractRemakerPlugin() {
        this.xmlForRemakingByElement = new HashMap<Long, OsmApiRoot>();
    }
    
    public OsmApiRoot getXmlForRemaking(float osmId) {
        return this.xmlForRemakingByElement.get(osmId);
    }

}
