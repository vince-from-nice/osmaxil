package org.openstreetmap.osmaxil.step;

import java.util.List;

import javax.annotation.PreDestroy;

import org.openstreetmap.osmaxil.Exception;
import org.openstreetmap.osmaxil.dao.ElementStore;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoadingStep  extends AbstractStep {

    private long counterForLoadedImports;
    
    private long counterForMatchedImports;
    
    @Autowired
    private ElementStore elementCache;
        
    @PreDestroy
    public void close() {
        LOGGER.info("=== Closing import org.openstreetmap.osmaxil.plugin.loader ===");
        LOGGER.info("Total of loaded imports: " + this.counterForLoadedImports);
        LOGGER.info("Total of matched imports: " + this.counterForMatchedImports);
    }

    public void loadImports() {
        LOGGER.info("=== Loading imports ===");
        LOGGER.info(LOG_SEPARATOR);
        while (this.plugin.getLoader().hasNext()) {
            try {
                    AbstractImport imp = (AbstractImport) this.plugin.getLoader().next();
                    this.counterForLoadedImports++;
                    this.loadImport(imp);
            } catch (java.lang.Exception e) {
                LOGGER.error("An import has failed: ", e);
            } finally {
                LOGGER.info(LOG_SEPARATOR);
            }
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
            // Skip negative IDs (ie. multipolygon relations whose outer member has not been found)
            if (osmId < 0) {
                break;
            }
            // Get related element from the cache or create it
            AbstractElement element = null;
            try {
                element = this.getOrCreateElement(relevantElementId);
            } catch (Exception e) {
                LOGGER.error("Skipping element id=" + osmId + " (" + e.getMessage() + ")");
                break;
            }
            LOGGER.info(element);
            // And bind the import to it
            this.bindImportToElement(element, imp);
        }
    }
    
    private AbstractElement getOrCreateElement(MatchingElementId relevantElementId) throws Exception {
        long osmId  = relevantElementId.getOsmId();
        AbstractElement element = this.elementCache.getElements().get(osmId);
        if (element == null) {
            // Fetch data from OSM API
            OsmApiRoot apiData = this.osmApiService.readElement(osmId);
            if (apiData == null) {
                throw new Exception("Unable to fetch data from OSM API for element#" + osmId);
            }
            element = (AbstractElement) this.plugin.instanciateElement(osmId, relevantElementId.getRelationId(), apiData);
            this.elementCache.getElements().put(osmId, element);
        } /*else {
            // If element was already present refresh its data
            element.setApiData(apiData);                
        }*/
        return element;
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
    }
    
}
