package org.openstreetmap.osmaxil.plugin.maker;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;

public abstract class AbstractMakerPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport> extends AbstractPlugin<ELEMENT, IMPORT> {

    protected OsmXmlRoot dataForCreation;
    
    abstract protected void processImport(IMPORT imp);

    abstract protected void buildDataForCreation();
    
    @Override
    public void process() {
        // TODO
    }
    
    @Override
    public void synchronize() {
        // TODO
    }

    public OsmXmlRoot getDataForCreation() {
        return dataForCreation;
    }
    
    @Override
    public void displayProcessingStatistics() {
        // TODO Auto-generated method stub
    }

    @Override
    public void displaySynchronizingStatistics() {
        // TODO Auto-generated method stub
    }
}
