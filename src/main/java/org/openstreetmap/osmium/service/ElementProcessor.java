package org.openstreetmap.osmium.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.AbstractImport;
import org.openstreetmap.osmium.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ElementProcessor {
    
    private long counter;

    @Autowired
    @Qualifier (value="OpenDataParisCsvPlugin")
    private AbstractPlugin pluginAutowiredBySpring;
 
    //@Autowired (value="OpenDataParisCsvPlugin")
    private AbstractPlugin<AbstractElement, AbstractImport> plugin;
    
    @Autowired
    private ElementCache elementCache;

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
    
    public void processElements() {
        LOGGER.info("=== Processing elements ===");
        LOGGER.info(LOG_SEPARATOR);
        try {
            for (AbstractElement element : this.elementCache.getElements().values()) {
                this.counter++;
                processElement(element);
                LOGGER.info(LOG_SEPARATOR);
            }
        } catch (java.lang.Exception e) {
            LOGGER.error("Element process has failed: ", e);
        }
    }
    
    private void processElement(AbstractElement element) {
        if (element == null) {
            LOGGER.warn("Element is null, skipping it...");
            return;
        }
        LOGGER.info("Processing element #" + this.counter + ": " +  element);
        // Basic processing
        this.findBestMatchingImport(element);
        // Better processing 
        this.dispatchMatchingImportsByTagValues(element);
        this.computeMatchingScoresByTagValues(element);
    }
    
    /**
     * For each "updatable" tag of the element, dispatch matching imports by their tag value.
     * 
     * @param element
     */
    private void dispatchMatchingImportsByTagValues(AbstractElement element) {
        // For each updatable tag names..
        for (String updatableTagName : element.getUpdatableTagNames()) {
            Map<String, List<AbstractImport>> map = element.getMatchingImportsByTagValueByTagName(updatableTagName);
            // For each matching import..
            for (AbstractImport imp : element.getMatchingImports()) {
                // Dispatch it by its tag value
                String updatableTagValue = element.getTagValue(updatableTagName);
                if (map.get(updatableTagValue) == null) {
                    map.put(updatableTagValue, new ArrayList<AbstractImport>());
                }
                map.get(updatableTagValue).add(imp);
            }
        }
    }
    
    /**
     * For each "updatable" tag of the element, compute matching scores for each tag value.
     * 
     * @param element
     */
    private void computeMatchingScoresByTagValues(AbstractElement element) {
        // For each updatable tag names..
        for (String updatableTagName : element.getUpdatableTagNames()) {
            LOGGER.info("Computing total scores by values for the tag " + updatableTagName);
            Map<String, List<AbstractImport>> map = element.getMatchingImportsByTagValueByTagName(updatableTagName);
            // For each updatable tag value..
            for (String updatableTagValue : map.keySet()) {
                // Compute matching score
                Float score = new Float(0);
                StringBuilder sb = new StringBuilder();
                for(AbstractImport imp : map.get(updatableTagValue)) {
                    score += imp.getMatchingScore();
                    sb.append("id=" + imp.getId() + " score=" + imp.getMatchingScore() + " ");
                }
                element.getTotalScoresByTagValueByTagName(updatableTagName).put(updatableTagValue, score);
                LOGGER.info(" - for value=" + updatableTagValue + " total score is " + score + " (" + sb.toString() + ")");
            }
        }
    }
    
    /**
     * Find the matching import with the best matching score
     * 
     * @param element
     */
    private void findBestMatchingImport(AbstractElement element) { 
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
