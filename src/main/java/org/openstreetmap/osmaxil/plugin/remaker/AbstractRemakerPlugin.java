package org.openstreetmap.osmaxil.plugin.remaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementWithParentFlags;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiRoot;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;

public abstract class AbstractRemakerPlugin <ELEMENT extends AbstractElement, IMPORT extends AbstractImport>  
    extends AbstractPlugin<ELEMENT, IMPORT>  {
    
    protected Map<Long, OsmApiRoot> newElementsCreationByElement;
    
    protected ArrayList<ElementWithParentFlags> elementsToDelete;
    
    abstract public void prepareRemaking(ELEMENT element);
    
    public AbstractRemakerPlugin() {
        this.newElementsCreationByElement = new HashMap<Long, OsmApiRoot>();
    }
    
    public OsmApiRoot getNewElementsCreation(long osmId) {
        return this.newElementsCreationByElement.get(osmId);
    }

}
