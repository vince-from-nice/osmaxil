package org.openstreetmap.osmium.service;

import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.Exception;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.AbstractImport;
import org.openstreetmap.osmium.data.RelevantElementId;
import org.openstreetmap.osmium.data.api.OsmApiRoot;
import org.openstreetmap.osmium.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ElementUpdater {
    
    private long counterForMatchedElements;
    
    private long counterForUpdatedElements;

    @Autowired
    private ElementCache elementCache;
    
    @Autowired
    @Qualifier (value="OpenDataParisCsvPlugin")
    private AbstractPlugin pluginAutowiredBySpring;
 
    //@Autowired (value="OpenDataParisCsvPlugin")
    private AbstractPlugin<AbstractElement, AbstractImport> plugin;

    @Autowired
    private OsmApiService osmApiService;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    static private final String LOG_SEPARATOR = "==========================================================";

    @PostConstruct
    public void init() {
        //TODO Autowire specialized plugin
        this.plugin = this.pluginAutowiredBySpring;
        this.osmApiService.init(this.plugin);
    }
    
    @PreDestroy
    public void close() {
        LOGGER.info("=== Closing element updater ===");
        LOGGER.info("Total of matched elements: " + this.counterForMatchedElements);
        LOGGER.info("Total of updated elements: " + this.counterForUpdatedElements);
    }
    
    public void updateElements() {
        LOGGER.info("=== Updating elements ===");
        LOGGER.info(LOG_SEPARATOR);
        try {
            for (AbstractElement element : this.elementCache.getElements().values()) {
                this.counterForMatchedElements++;
                updateElement(element);
                LOGGER.info(LOG_SEPARATOR);
            }
        } catch (java.lang.Exception e) {
            LOGGER.error("Import has failed: ", e);
        }
    }
    
    private void updateElement(AbstractElement element) {
        if (element == null) {
            LOGGER.warn("Element is null, skipping it...");
            return;
        }
        LOGGER.info("Processing element #" + this.counterForMatchedElements + ": " +  element);
        // Try to update the element data with the best matching element
        boolean needToUpdate = this.plugin.updateElementData(element.getBestMatchingImport(), element);
        // Update element only if needed
        if (needToUpdate) {
            if (this.osmApiService.writeElement(element)) {
                this.counterForUpdatedElements++;
            }
            LOGGER.debug("Ok element has been updated with import #" + element.getBestMatchingImport().getId());
        } else {
            LOGGER.info("Element cannot be modified (original values exist)");
        }
    }

}
