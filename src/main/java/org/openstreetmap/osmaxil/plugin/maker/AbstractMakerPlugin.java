package org.openstreetmap.osmaxil.plugin.maker;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.openstreetmap.osmaxil.util.IdIncrementor;

public abstract class AbstractMakerPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport> extends AbstractPlugin<ELEMENT, IMPORT> {

    // =========================================================================
    // Instance variables
    // =========================================================================
    
    //protected Map<Long, IMPORT> makableImports = new Hashtable<Long, IMPORT>();
    
    protected OsmXmlRoot dataForCreation;
    
    IdIncrementor idGenerator = new IdIncrementor(1);
    
    // =========================================================================
    // Abstract methods
    // =========================================================================
    
    abstract protected void processImport(IMPORT imp);

    abstract protected void buildDataForCreation();
    
    // =========================================================================
    // Public methods
    // =========================================================================
    
    @Override
    public void process() {
        int importNbr = 0;
        for (IMPORT imp : this.loadedImports) {
            LOGGER.info("Binding import #" + importNbr + ": " + imp);
            if (imp == null) {
                LOGGER.warn("Import is null, skipping it...");
                break;
            }
            this.processImport(imp);
            LOGGER.info(LOG_SEPARATOR);
        }
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
