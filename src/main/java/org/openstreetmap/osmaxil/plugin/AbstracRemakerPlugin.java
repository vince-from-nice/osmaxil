package org.openstreetmap.osmaxil.plugin;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;

public abstract class AbstracRemakerPlugin <Element extends AbstractElement, Import extends AbstractImport>  
    extends AbstractPlugin<Element, Import>  {
    
    abstract public void buildRemakedElements(Element element);

}
