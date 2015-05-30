package org.openstreetmap.osmaxil.step;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.dao.ElementStore;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.plugin.remaker.AbstractRemakerPlugin;
import org.openstreetmap.osmaxil.plugin.updater.AbstractUpdaterPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsStep extends AbstractStep {

    private int matchedElementsNbr;

    private int updatableElementsNbr;

    private int updatedElementsNbr;

    private int[] matchedElementsNbrByScore;

    private int[] updatableElementsNbrByScore;

    private int[] updatedElementsNbrByScore;

    @Autowired
    private ElementStore elementCache;
    
    static private final Logger LOGGER = Logger.getLogger(StatisticsStep.class);

    public void generateStats() {
        LOGGER.info("=== Statistics ===");
        if (this.plugin instanceof AbstractUpdaterPlugin) {
            this.buildUpdatingStats();
            this.displayUpdatingStats();
        } else if (this.plugin instanceof AbstractRemakerPlugin) {
            // TODO
        } 
        //displayAllMatchingScore();
    }
    
    private void displayAllMatchingScore() {
        LOGGER.info("Here are all matching scores:");
        for (AbstractElement element : elementCache.getElements().values()) {
            LOGGER.info("- score for element " + element.getOsmId() + " is " + element.getMatchingScore());
        }
    }
    
    private void displayUpdatingStats() {
        LOGGER.info("Number of matched elements: " + this.matchedElementsNbr);
        LOGGER.info("Number of updatable elements: " + this.updatableElementsNbr);
        LOGGER.info("Number of updated elements: " + this.updatedElementsNbr);
        LOGGER.info("Repartition by matching scores:");
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("- score between " + i * 10 + "% and " + (i + 1) * 10 + "% : ");
            sb.append(this.matchedElementsNbrByScore[i]);
            if (this.elementCache.getElements().size()  > 0) {
                sb.append(" (" + 100 * this.matchedElementsNbrByScore[i] / this.elementCache.getElements().size() + "%)");
            }
            sb.append(" elements <= " + this.updatedElementsNbrByScore[i] + " updated");
            sb.append(" (" + this.updatableElementsNbrByScore[i] + " were updatable)");
            LOGGER.info(sb);
        }
    }

    private void buildUpdatingStats() {
        this.matchedElementsNbr = 0;
        this.updatableElementsNbr = 0;
        this.updatedElementsNbr = 0;
        this.matchedElementsNbrByScore = new int[10];
        this.updatedElementsNbrByScore = new int[10];
        this.updatableElementsNbrByScore = new int[10];
        for (AbstractElement element : this.elementCache.getElements().values()) {
            Float score = element.getMatchingScore();
            if (score == null) {
                LOGGER.warn("Element " + element.getOsmId() + " doesn't have matching score !!");
            } else {
                boolean ok = false;
                for (int i = 0; i < 10; i++) {
                    if (score <= (i + 1) * 0.1) {
                        ok = true;
                        this.matchedElementsNbr++;
                        this.matchedElementsNbrByScore[i]++;
                        if (element.isUpdated()) {
                            this.updatedElementsNbr++;
                            this.updatedElementsNbrByScore[i]++;
                        }
                        // TODO move it to UpdaterPlugin
                        boolean updatable = false;
                        AbstractUpdaterPlugin updaterPlugin = (AbstractUpdaterPlugin) this.plugin;
                        for (int j = 0; j < updaterPlugin.getUpdatableTagNames().length; j++) {
                            updatable = updaterPlugin.isElementTagUpdatable(element, updaterPlugin.getUpdatableTagNames()[j]);
                        }
                        if (updatable) {
                            this.updatableElementsNbr++;
                            this.updatableElementsNbrByScore[i]++;
                        }
                        break;
                    }                    
                }
                if (!ok) {
                    LOGGER.error("Stats issue with element " + element.getOsmId() + ", its matching score is " + element.getMatchingScore());
                }
            }
        }
    }

}