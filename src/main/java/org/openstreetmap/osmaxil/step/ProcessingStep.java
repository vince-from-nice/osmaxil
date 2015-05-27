package org.openstreetmap.osmaxil.step;

import org.openstreetmap.osmaxil.dao.ElementStore;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstracRemakerPlugin;
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
            imp.setMatchingScore(this.plugin.computeImportMatchingScore(imp));
        }
        // Compute a global matching score for the element
        element.setMatchingScore(this.plugin.computeElementMatchingScore(element));
        // Do specific stuff depending on the plugin
        if (this.plugin instanceof AbstracRemakerPlugin) {
            // Create remaked elements
            ((AbstracRemakerPlugin) this.plugin).buildXmlForRemaking(element);
        }
    }

}
