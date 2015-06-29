package org.openstreetmap.osmaxil.step;

import org.openstreetmap.osmaxil.dao.ElementStore;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.remaker.AbstractRemakerPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessingStep extends AbstractStep {

    private long counter;

    @Autowired
    private ElementStore elementCache;
    
    @Override
    public void displayStats() {
        LOGGER_FOR_STATS.info("=== Statistics for " + this.getClass().getSimpleName() + "===");
        LOGGER_FOR_STATS.info("Total of processed elements: " + this.counter);
        LOGGER_FOR_STATS.info("Plugin specific statistics:");
        this.plugin.displayProcessingStatistics();
    }

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
        // Do specific stuff depending on the plugin
        if (this.plugin instanceof AbstractRemakerPlugin) {
            ((AbstractRemakerPlugin) this.plugin).buildRemakingData();
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
            imp.setMatchingScore(this.plugin.computeImportMatchingScore(imp));
        }
        // Compute a global matching score for the element
        element.setMatchingScore(this.plugin.computeElementMatchingScore(element));
        // Do specific stuff depending on the plugin
        if (this.plugin instanceof AbstractRemakerPlugin) {
            AbstractRemakerPlugin abstractPlugin = (AbstractRemakerPlugin) this.plugin;
            // Prepare remaking data for this element only if it is alterable
            if (abstractPlugin.isElementAlterable(element)) {
                abstractPlugin.processElement(element);
            }
        }
    }

}
