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
public class ElementMatcher {
    
    private long counter;
    
    @Autowired
    private ElementCache elementCache;

    @Autowired
    private OsmApiService osmApiService;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    static private final String LOG_SEPARATOR = "==========================================================";
    
    public void processElements() {
        LOGGER.info("=== Matching elements ===");
        LOGGER.info(LOG_SEPARATOR);
        try {
            for (AbstractElement element : this.elementCache.getElements().values()) {
                this.counter++;
                processElement(element);
                LOGGER.info(LOG_SEPARATOR);
            }
        } catch (java.lang.Exception e) {
            LOGGER.error("Import has failed: ", e);
        }
    }
    
    private void processElement(AbstractElement element) {
        if (element == null) {
            LOGGER.warn("Element is null, skipping it...");
            return;
        }
        LOGGER.info("Processing element #" + this.counter + ": " +  element);
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
    }

}
