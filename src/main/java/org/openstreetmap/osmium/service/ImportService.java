package org.openstreetmap.osmium.service;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.AbstractImport;
import org.openstreetmap.osmium.data.RelevantElementId;
import org.openstreetmap.osmium.data.api.OsmApiRoot;
import org.openstreetmap.osmium.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ImportService {

    private long counterForLoadedImports;
    
    private long counterForMatchedImports;

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
        LOGGER.info("=== Closing Import service ===");
        LOGGER.info("Total of loaded imports: " + this.counterForLoadedImports);
        LOGGER.info("Total of matched imports: " + this.counterForMatchedImports);
        LOGGER.info("Total of matched elements: " + this.elements.size());
        LOGGER.info("Total of updated elements: " + this.updatedElements.size());
    }

    public void importBuildings() {
        this.osmApiService.init(this.plugin);
        LOGGER.info(LOG_SEPARATOR);
        try {
            while (this.plugin.hasNext()) {
                AbstractImport imp = (AbstractImport) this.plugin.next();
                this.counterForLoadedImports++;
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
        LOGGER.info("Importing element #" + counterForLoadedImports + ": " +  imp);
        // Find relevant element
        List<RelevantElementId> relevantElementIds = this.plugin.findRelevantElements(imp);
        if (relevantElementIds.size() > 0) {
            this.counterForMatchedImports++;
        }
        // For each matching elements
        for (RelevantElementId relevantElementId : relevantElementIds) {
            long osmId = relevantElementId.getOsmId();
            long relationId = relevantElementId.getRelationId();
            // Skip negative IDs (ie. multipolygon relations whose outer member has not been found)
            if (osmId < 0) {
                break;
            }
            // Fetch data from OSM API
            OsmApiRoot apiData = this.osmApiService.readElement(osmId);
            if (apiData == null) {
                LOGGER.info("Skipping element id=" + osmId + " since no data has been fetch from OSM API");
                break;
            }
            // Get related element from the private map (ie. cache) or create it
            AbstractElement element = (AbstractElement) this.elements.get(osmId);
            if (element == null) {
                element = (AbstractElement) this.plugin.createElement(osmId, relationId, apiData);
                this.elements.put(osmId, element);
            } else {
                // If element was already present refresh its data
                element.setApiData(apiData);                
            }
            LOGGER.info(element);
            // Bind import to element
            boolean needToUpdate = this.bindImportToElement(element, imp);
            // Update element only if needed
            if (needToUpdate) {
                if (this.osmApiService.writeElement(element)) {
                    updatedElements.put(element.getOsmId(), element);
                }
            }
        }
    }
    
    private boolean bindImportToElement(AbstractElement element, AbstractImport imp) {
        // Attach import to the element
        element.getMatchingImports().add(imp);
        imp.setElement(element); 
        StringBuilder sb = new StringBuilder("Matching imports are now : [ ");
        for (AbstractImport i : element.getMatchingImports()) {
            sb.append(i.getId() + " ");
        }
        LOGGER.info(sb.append("]").toString());
        // Compute matching score for the import
        imp.setMatchingScore(this.plugin.computeMatchingScore(imp));
        // Check if that import is the new winner or a looser
        boolean needToUpdate = false;
        AbstractImport bestImport = element.getBestMatchingImport();
        sb = new StringBuilder("New import score is " + imp.getMatchingScore() + " and best matching import score is ");
        sb.append(bestImport != null ? bestImport.getMatchingScore() + " (id=" + bestImport.getId() + ")" : "null");
        // If that import has better score (or it's the first import matching the element), it's a winner
        if (element.getBestMatchingImport() == null
                || element.getBestMatchingImport().getMatchingScore() < imp.getMatchingScore()) {
            sb.append(" => We have a new winner !!");
            LOGGER.info(sb.toString());
            element.setBestMatchingImport(imp);
            // Try to update the element data with it
            needToUpdate = this.plugin.updateApiData(imp, element);
        }
        // Else it's a looser, nothing to do.. 
        else {
            sb.append(" => Loosing import");
            LOGGER.info(sb.toString());
        }
        if (needToUpdate) {
            LOGGER.info("Element has been modified");
        } else {
            LOGGER.info("Element has NOT been modified");
        }
        return needToUpdate;
    }

}
