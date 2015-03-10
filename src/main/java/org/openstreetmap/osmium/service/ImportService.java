package org.openstreetmap.osmium.service;

import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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

    private long counterForImports;

    private Map<Long, AbstractElement> elements;
    
    private Map<Long, AbstractElement> updatedElements;
    
    @Autowired
    @Qualifier (value="OpenDataParisCsvPlugin")
    private AbstractPlugin pluginAutowiredBySpring;
 
    //@Autowired (value="OpenDataParisCsvPlugin")
    private AbstractPlugin<AbstractElement, AbstractImport> plugin;

    @Autowired
    private OsmApiService osmApiService;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    static private final String LOG_SEPARATOR = "==========================================================";

    public ImportService() throws Exception {
        this.elements = new Hashtable<Long, AbstractElement>();
        this.updatedElements = new Hashtable<Long, AbstractElement>();
    }
    
    @PostConstruct
    public void init() {
        //TODO Autowire specialized plugin
        this.plugin = this.pluginAutowiredBySpring;
    }
    
    @PreDestroy
    public void close() {
        LOGGER.info("Closing Import service");
        LOGGER.info("Total of loaded imports: " + this.counterForImports);
        LOGGER.info("Total of matched elements: " + this.elements.size());
        LOGGER.info("Total of updated elements: " + this.updatedElements.size());
    }

    public void importBuildings() {
        this.osmApiService.init(this.plugin);
        LOGGER.info(LOG_SEPARATOR);
        try {
            while (this.plugin.hasNext()) {
                AbstractImport imp = (AbstractImport) this.plugin.next();
                this.counterForImports++;
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
        LOGGER.info("Importing element #" + counterForImports + ": " +  imp);
        // For each matching elements
        Long[] osmIds = this.plugin.findRelatedElementId(imp);
        for (Long osmId : osmIds) {
            // Fetch data from OSM API
            OsmApiRoot apiData = this.osmApiService.readElement(osmId);
            if (apiData == null) {
                LOGGER.info("Skipping element id=[+ " + osmId + "] since no data has been fetch from OSM API");
                break;
            }
            // Get related element from the map or create it
            AbstractElement element = (AbstractElement) this.elements.get(osmId);
            if (element == null) {
                element = (AbstractElement) this.plugin.createElement(osmId, apiData);
                this.elements.put(osmId, element);
            } else {
                // If element was already present refresh its data
                element.setApiData(apiData);                
            }
            LOGGER.info(element);
            // Bind import to element
            boolean needToUpdate = this.plugin.bindImportToElement(element, imp);
            // Update element only if needed
            if (needToUpdate) {
                if (this.osmApiService.writeElement(element)) {
                    updatedElements.put(element.getOsmId(), element);
                }
            }
        }
    }

}