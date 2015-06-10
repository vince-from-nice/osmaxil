package org.openstreetmap.osmaxil.plugin.remaker;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiRoot;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;

public abstract class AbstractRemakerPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
        extends AbstractPlugin<ELEMENT, IMPORT> {

    protected List<ELEMENT> remakableElements = new ArrayList<>();
    
    protected OsmApiRoot remakingData;

    abstract public void prepareRemakingDataByElement(ELEMENT element);

    abstract public void finalizeRemakingData();

    public OsmApiRoot getRemakingData() {
        return remakingData;
    }

}
