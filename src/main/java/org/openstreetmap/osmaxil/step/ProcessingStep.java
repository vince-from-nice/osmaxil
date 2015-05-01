package org.openstreetmap.osmaxil.step;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.dao.ElementStore;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstracRemakerPlugin;
import org.openstreetmap.osmaxil.plugin.AbstractUpdaterPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessingStep extends AbstractStep {

    private long counter;

    @Autowired
    private ElementStore elementCache;

    public void processElements() {
        LOGGER.info("=== Processing elements ===");
        LOGGER.info(LOG_SEPARATOR);
        for (AbstractElement element : this.elementCache.getElements().values()) {
            this.counter++;
            try {
                processElement(element);
            } catch (java.lang.Exception e) {
                LOGGER.error("Process of element " + element.getOsmId() + " has failed: ", e);
            }
            LOGGER.info(LOG_SEPARATOR);
        }
    }

    private void processElement(AbstractElement element) {
        if (element == null) {
            LOGGER.warn("Element is null, skipping it...");
            return;
        }
        LOGGER.info("Processing element #" + this.counter + ": " + element);
        // Compute a matching score for each import matching the element
        for (AbstractImport imp : element.getMatchingImports()) {
            imp.setMatchingScore(this.plugin.computeMatchingScore(imp));
        }
        // If the plugin is an updater, need to do additional stuff
        if (this.plugin instanceof AbstractUpdaterPlugin) {
            // For the old basic best matching method:
            this.findBestMatchingImport(element);
            // For the new extended matching method:
            this.dispatchMatchingImportsByTagValues(element);
            this.computeTotalScoresByTagValues(element);            
        } else if (this.plugin instanceof AbstracRemakerPlugin) {
            // TODO processing stuff for creation ?
        }
    }
    
    /**
     * For each "updatable" tag of the plugin, dispatch matching imports of the element by their tag value.
     * 
     * @param element
     */
    @Obsolete
    private void dispatchMatchingImportsByTagValues(AbstractElement element) {
        // For each updatable tag names..
        for (String updatableTagName : ((AbstractUpdaterPlugin) this.plugin).getUpdatableTagNames()) {
            Map<String, List<AbstractImport>> map = element.getMatchingImportsByTagValuesByTagName(updatableTagName);
            // For each matching import..
            for (AbstractImport imp : element.getMatchingImports()) {
                // Dispatch it by its tag value
                String updatableTagValue = imp.getTagValue(updatableTagName);
                // String updatableTagValue = element.getTagValue(updatableTagName);
                if (map.get(updatableTagValue) == null) {
                    map.put(updatableTagValue, new ArrayList<AbstractImport>());
                }
                map.get(updatableTagValue).add(imp);
            }
        }
    }

    /**
     * For each "updatable" tag of the plugin, compute matching scores for each tag value. At the end the element stores
     * accumulated scores of imports which have been aggregated by their tag value.
     * 
     * @param element
     */
    private void computeTotalScoresByTagValues(AbstractElement element) {
        // For each updatable tag names..
        for (String updatableTagName : ((AbstractUpdaterPlugin) this.plugin).getUpdatableTagNames()) {
            LOGGER.info("Computing total scores by values for the tag " + updatableTagName);
            Map<String, List<AbstractImport>> map = element.getMatchingImportsByTagValuesByTagName(updatableTagName);
            // For each updatable tag value..
            for (String updatableTagValue : map.keySet()) {
                // Compute matching score
                Float score = new Float(0);
                StringBuilder sb = new StringBuilder();
                for (Iterator<AbstractImport> iterator = map.get(updatableTagValue).iterator(); iterator.hasNext();) {
                    AbstractImport imp = (AbstractImport) iterator.next();
                    score += imp.getMatchingScore();
                    sb.append("" + imp.getMatchingScore() + " (id=" + imp.getId() + ")");
                    if (iterator.hasNext()) {
                        sb.append(" + ");
                    }
                }
                element.getTotalScoresByTagValuesByTagName(updatableTagName).put(updatableTagValue, score);
                LOGGER.info(" - for value=" + updatableTagValue + " total score is " + score + " = " + sb.toString());
            }
        }
    }

    /**
     * Find the matching import with the best matching score
     * 
     * @param element
     */
    private void findBestMatchingImport(AbstractElement element) {
        for (AbstractImport imp : element.getMatchingImports()) {
            // Check if that import is the new winner or a looser
            AbstractImport bestImport = element.getBestMatchingImport();
            StringBuilder sb = new StringBuilder("Import #" + imp.getId() + " has a score of " + imp.getMatchingScore()
                    + " and best matching import score is ");
            sb.append(bestImport != null ? bestImport.getMatchingScore() + " (id=" + bestImport.getId() + ")" : "null");
            // If that import has better score (or it's the first import matching the element), it's a winner
            if (element.getBestMatchingImport() == null
                    || element.getBestMatchingImport().getMatchingScore() < imp.getMatchingScore()) {
                sb.append(" => We have a new winner !!");
                //LOGGER.info(sb.toString());
                element.setBestMatchingImport(imp);
            }
            // Else it's a looser, nothing to do..
            else {
                sb.append(" => Loosing import");
                //LOGGER.info(sb.toString());
            }
        }
    }

}
