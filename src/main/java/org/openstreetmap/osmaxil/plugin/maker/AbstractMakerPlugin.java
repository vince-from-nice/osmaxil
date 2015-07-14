package org.openstreetmap.osmaxil.plugin.maker;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;

public abstract class AbstractMakerPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport> extends AbstractPlugin<ELEMENT, IMPORT> {

    protected OsmXmlRoot dataForCreation;
    
    abstract public void processElement(ELEMENT element);

    abstract protected void buildDataForCreation();
    
    @Override
    public void process() {
        // TODO
    }
    
    @Override
    public void synchronize() {
        // TODO
    }
    
    public void buildMakingData() {
        this.buildDataForCreation();    }

    public OsmXmlRoot getDataForCreation() {
        return dataForCreation;
    }
}
