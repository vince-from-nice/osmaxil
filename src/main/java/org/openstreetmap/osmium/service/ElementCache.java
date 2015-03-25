package org.openstreetmap.osmium.service;

import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.OsmiumException;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.AbstractImport;
import org.openstreetmap.osmium.data.RelevantElementId;
import org.openstreetmap.osmium.data.api.OsmApiRoot;
import org.openstreetmap.osmium.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ElementCache {
    
    private Map<Long, AbstractElement> elements;
    
    private long counterForMatchedElements;
    
    private long counterForUpdatedElements;
    
    @Autowired
    @Qualifier (value="OpenDataParisCsvPlugin")
    private AbstractPlugin pluginAutowiredBySpring;
 
    //@Autowired (value="OpenDataParisCsvPlugin")
    private AbstractPlugin<AbstractElement, AbstractImport> plugin;

    @Autowired
    private OsmApiService osmApiService;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    static private final String LOG_SEPARATOR = "==========================================================";
    
    public ElementCache() throws Exception {
        this.elements = new Hashtable<Long, AbstractElement>();
    }
    
    @PostConstruct
    public void init() {
        //TODO Autowire specialized plugin
        this.plugin = this.pluginAutowiredBySpring;
        this.osmApiService.init(this.plugin);
    }
    
    @PreDestroy
    public void close() {
        LOGGER.info("=== Closing element cache ===");
        LOGGER.info("Total of matched elements: " + this.counterForMatchedElements);
        LOGGER.info("Total of updated elements: " + this.counterForUpdatedElements);
    }
    
    public AbstractElement getOrCreateElement(RelevantElementId relevantElementId) throws OsmiumException {
        long osmId  = relevantElementId.getOsmId();
        AbstractElement element = this.elements.get(osmId);
        if (element == null) {
            // Fetch data from OSM API
            OsmApiRoot apiData = this.osmApiService.readElement(osmId);
            if (apiData == null) {
                throw new OsmiumException("Unable to fetch data from OSM API for element#" + osmId);
            }
            element = (AbstractElement) this.plugin.createElement(osmId, relevantElementId.getRelationId(), apiData);
            this.elements.put(osmId, element);
        }/* else {
            // If element was already present refresh its data
            element.setApiData(apiData);                
        }*/
        return element;
    }
    
    public void processElements() {
        LOGGER.info("=== Processing elements ===");
        LOGGER.info(LOG_SEPARATOR);
        try {
            for (AbstractElement element : this.elements.values()) {
                this.counterForMatchedElements++;
                processElement(element);
                LOGGER.info(LOG_SEPARATOR);
            }
        } catch (Exception e) {
            LOGGER.error("Import has failed: ", e);
        }
    }
    
    private void processElement(AbstractElement element) {
        if (element == null) {
            LOGGER.warn("Element is null, skipping it...");
            return;
        }
        LOGGER.info("Processing element #" + this.counterForMatchedElements + ": " +  element);
        // Parsing all imports binded to the element
        for(AbstractImport imp : element.getMatchingImports()) {
            // Check if that import is the new winner or a looser
            AbstractImport bestImport = element.getBestMatchingImport();
            StringBuilder sb = new StringBuilder("Import #" + imp.getId() + " has a score of " + imp.getMatchingScore() + " and best matching import score is ");
            sb.append(bestImport != null ? bestImport.getMatchingScore() + " (id=" + bestImport.getId() + ")" : "null");
            // If that import has better score (or it's the first import matching the element), it's a winner
            if (element.getBestMatchingImport() == null
                    || element.getBestMatchingImport().getMatchingScore() < imp.getMatchingScore()) {
                sb.append(" => We have a new winner !!");
                LOGGER.info(sb.toString());
                element.setBestMatchingImport(imp);
            }
            // Else it's a looser, nothing to do.. 
            else {
                sb.append(" => Loosing import");
                LOGGER.info(sb.toString());
            }
        }
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
