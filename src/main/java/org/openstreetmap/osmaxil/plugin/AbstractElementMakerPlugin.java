package org.openstreetmap.osmaxil.plugin;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;

public abstract class AbstractElementMakerPlugin <Element extends AbstractElement, Import extends AbstractImport>  
    extends AbstractPlugin<Element, Import>  {

    abstract public Element instanciateElement(long osmId);
    
    public Element instanciateElement(long osmId, long relationId, OsmApiRoot data) {
        Element element = instanciateElement(osmId);
        element.setRelationId(relationId);
        element.setApiData(data);
        return element;
    }
}
