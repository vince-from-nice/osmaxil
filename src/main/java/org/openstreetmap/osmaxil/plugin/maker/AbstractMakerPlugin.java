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
    
    protected OsmXmlRoot data;
    
    IdIncrementor idGenerator = new IdIncrementor(1);
    
    private int counterForMakableImports;

    private int counterForMakedImports;
    
    // =========================================================================
    // Abstract methods
    // =========================================================================
    
    abstract protected boolean isImportMakable(IMPORT imp); 
    
    abstract protected void processImport(IMPORT imp);

    abstract protected void buildData();
    
    // =========================================================================
    // Public methods
    // =========================================================================
    
    @Override
    public void process() {
        int importNbr = 0;
        for (IMPORT imp : this.loadedImports) {
            LOGGER.info("Processing import #" + importNbr + ": " + imp);
            if (imp == null) {
                LOGGER.warn("Import is null, skipping it...");
                break;
            }
            if (this.isImportMakable(imp)) {
                this.processImport(imp);
                this.counterForMakableImports++;
            }
            LOGGER.info(LOG_SEPARATOR);
        }
        this.buildData();
    }
    
    @Override
    public void synchronize() {
        boolean success = false;
        if (this.data == null) {
            LOGGER.warn("Unable to synchronize because data is null");
            return;
        }
        if ("api".equals(this.synchronizationMode)) {
            // TODO direct api writing for making
        } else if ("gen".equals(this.synchronizationMode)) {
            success = this.osmXmlFile.writeToFile("genfile", this.data);
        }
        if (success) {
            LOGGER.info("Ok all imports has been synchronized");
            this.counterForMakedImports++;
        }
    }
    
    public OsmXmlRoot getDataForCreation() {
        return data;
    }
    
    @Override
    public void displayProcessingStatistics() {
        LOGGER_FOR_STATS.info("=== Processing statistics ===");
        LOGGER_FOR_STATS.info("Total of makable imports: " + this.counterForMakableImports);
    }

    @Override
    public  void displaySynchronizingStatistics(){
        LOGGER_FOR_STATS.info("=== Synchronizing statistics ===");
        LOGGER_FOR_STATS.info("Total of maked imports: " + this.counterForMakedImports);
    }
}
