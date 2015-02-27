package org.openstreetmap.osmium.service;

import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.AbstractImport;
import org.openstreetmap.osmium.data.api.OsmApiRoot;
import org.openstreetmap.osmium.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ImportService {

    private long counter;

    private Map<Long, AbstractElement> elements;

    // TODO Autowire specialized plugin
    //@Autowired (value="OpenDataParisCsvPlugin")
    private AbstractPlugin<AbstractElement, AbstractImport> plugin;

    @Autowired
    @Qualifier (value="OpenDataParisCsvPlugin")
    private AbstractPlugin pluginAutowiredBySpring;
    
    @Autowired
    private OsmApiService osmApiService;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    static private final String LOG_SEPARATOR = "==========================================================";

    public ImportService() throws Exception {
        this.elements = new Hashtable<Long, AbstractElement>();
    }
    
    @PostConstruct
    public void init() {
        this.plugin = this.pluginAutowiredBySpring;
    }

    public void importBuildings() {
        LOGGER.info(LOG_SEPARATOR);
        try {
            while (this.plugin.hasNext()) {
                AbstractImport imp = (AbstractImport) this.plugin.next();
                this.counter++;
                this.processImport(imp);
                LOGGER.info(LOG_SEPARATOR);
            }
        } catch (Exception e) {
            LOGGER.error("Import has failed: ", e);
        }
    }

    private void processImport(AbstractImport imp) {
        if (imp == null) {
            LOGGER.warn("Element import is null, skipping import...");
            return;
        }
        LOGGER.info("Importing element #" + counter + ": " +  imp);
        Long[] osmIds = this.plugin.findRelatedElementId(imp);
        for (Long osmId : osmIds) {
            // Get related element from the map or create it
            AbstractElement element = (AbstractElement) this.elements.get(osmId);
            if (element == null) {
                element = (AbstractElement) this.plugin.createElement(osmId);
                this.elements.put(osmId, element);
            }
            // Refresh data from API even if element was already present
            OsmApiRoot apiData = this.osmApiService.readElement(osmId);
            if (apiData == null) {
                LOGGER.info("Skipping element id=[+ " + osmId + "] since no data has been fetch from OSM API");
                break;
            }
            element.setApiData(apiData);
            LOGGER.info(element);
            // If original XML has already some values we skip the import
            /*if (!element.isVirgin()) {
                LOGGER.info("Skipping element id=[" + osmId + "] since element is not virgin");
                break;
            }*/
            // Bind import to element
            boolean needToUpdate = this.plugin.bindImportToElement(element, imp);
            // Write element only if needed
            if (needToUpdate) {
                this.osmApiService.writeElement(element);
            }
        }
    }

}
