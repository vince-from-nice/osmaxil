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

    private int alterableElementsNbr;

    private int alteratedElementsNbr;

    private int[] matchedElementsNbrByScore;

    private int[] alterableElementsNbrByScore;

    private int[] alteredElementsNbrByScore;

    @Autowired
    private ElementStore elementCache;
    
    static private final Logger LOGGER = Logger.getLogger(StatisticsStep.class);

    public void generateStats() {
        LOGGER.info("=== Statistics ===");
        this.buildStats();
        this.displayStats();
        //displayAllMatchingScore();
    }
    
    private void displayAllMatchingScore() {
        LOGGER.info("Here are all matching scores:");
        for (AbstractElement element : elementCache.getElements().values()) {
            LOGGER.info("- score for element " + element.getOsmId() + " is " + element.getMatchingScore());
        }
    }
    
    private void displayStats() {
        LOGGER.info("Number of matched elements: " + this.matchedElementsNbr);
        LOGGER.info("Number of updatable elements: " + this.alterableElementsNbr);
        LOGGER.info("Number of updated elements: " + this.alteratedElementsNbr);
        LOGGER.info("Repartition by matching scores:");
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("- score between " + i * 10 + "% and " + (i + 1) * 10 + "% : ");
            sb.append(this.matchedElementsNbrByScore[i]);
            if (this.elementCache.getElements().size()  > 0) {
                sb.append(" (" + 100 * this.matchedElementsNbrByScore[i] / this.elementCache.getElements().size() + "%)");
            }
            sb.append(" elements <= " + this.alteredElementsNbrByScore[i] + " updated");
            sb.append(" (" + this.alterableElementsNbrByScore[i] + " were updatable)");
            LOGGER.info(sb);
        }
    }

    private void buildStats() {
        this.matchedElementsNbr = 0;
        this.alterableElementsNbr = 0;
        this.alteratedElementsNbr = 0;
        this.matchedElementsNbrByScore = new int[10];
        this.alteredElementsNbrByScore = new int[10];
        this.alterableElementsNbrByScore = new int[10];
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
                        if (element.isAltered()) {
                            this.alteratedElementsNbr++;
                            this.alteredElementsNbrByScore[i]++;
                        }
                        if (this.plugin.isElementAlterable(element)) {
                            this.alterableElementsNbr++;
                            this.alterableElementsNbrByScore[i]++;
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