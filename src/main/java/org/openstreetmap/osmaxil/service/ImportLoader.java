package org.openstreetmap.osmaxil.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.Exception;
import org.openstreetmap.osmaxil.data.AbstractElement;
import org.openstreetmap.osmaxil.data.AbstractImport;
import org.openstreetmap.osmaxil.data.MatchingElementId;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ImportLoader {

    private long counterForLoadedImports;
    
    private long counterForMatchedImports;
    
    @Autowired
    @Qualifier (value="OpenDataParisBuildingPlugin")
    private AbstractPlugin pluginAutowiredBySpring;
 
    //@Autowired (value="OpenDataParisBuildingPlugin")
    private AbstractPlugin<AbstractElement, AbstractImport> plugin;

    @Autowired
    private ElementCache elementCache;
    
    static private final Logger LOGGER = Logger.getLogger(Application.class);

    static private final String LOG_SEPARATOR = "==========================================================";

    @PostConstruct
    public void init() {
        //TODO Autowire specialized plugin
        this.plugin = this.pluginAutowiredBySpring;
    }
    
    @PreDestroy
    public void close() {
        LOGGER.info("=== Closing import loader ===");
        LOGGER.info("Total of loaded imports: " + this.counterForLoadedImports);
        LOGGER.info("Total of matched imports: " + this.counterForMatchedImports);
    }

    public void loadImports() {
        LOGGER.info("=== Loading imports ===");
        LOGGER.info(LOG_SEPARATOR);
        try {
            while (this.plugin.hasNext()) {
                AbstractImport imp = (AbstractImport) this.plugin.next();
                this.counterForLoadedImports++;
                this.loadImport(imp);
                LOGGER.info(LOG_SEPARATOR);
            }
        } catch (java.lang.Exception e) {
            LOGGER.error("Import has failed: ", e);
        }
    }
 
    private void loadImport(AbstractImport imp) {
        if (imp == null) {
            LOGGER.warn("Import is null, skipping it...");
            return;
        }
        LOGGER.info("Loading import #" + this.counterForLoadedImports + ": " +  imp);
        // Find relevant element
        List<MatchingElementId> relevantElementIds = this.plugin.findMatchingElements(imp);
        if (relevantElementIds.size() > 0) {
            this.counterForMatchedImports++;
        }
        // For each matching elements
        for (MatchingElementId relevantElementId : relevantElementIds) {
            long osmId = relevantElementId.getOsmId();
            long relationId = relevantElementId.getRelationId();
            // Skip negative IDs (ie. multipolygon relations whose outer member has not been found)
            if (osmId < 0) {
                break;
            }
            // Get related element from the cache or create it
            AbstractElement element = null;
            try {
                element = this.elementCache.getOrCreateElement(relevantElementId);
            } catch (Exception e) {
                LOGGER.error("Skipping element id=" + osmId + " (" + e.getMessage() + ")");
                break;
            }
            LOGGER.info(element);
            // Bind import to element
            this.bindImportToElement(element, imp);
        }
    }
    
    private void bindImportToElement(AbstractElement element, AbstractImport imp) {
        // Attach import to the element
        element.getMatchingImports().add(imp);
        imp.setElement(element); 
        StringBuilder sb = new StringBuilder("Matching imports are now : [ ");
        for (AbstractImport i : element.getMatchingImports()) {
            sb.append(i.getId() + " ");
        }
        LOGGER.info(sb.append("]").toString());
        // Compute matching score for the import
        imp.setMatchingScore(this.plugin.computeImportMatchingScore(imp));
    }

}
