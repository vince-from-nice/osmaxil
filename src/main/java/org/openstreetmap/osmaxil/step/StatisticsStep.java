package org.openstreetmap.osmaxil.step;

import org.openstreetmap.osmaxil.dao.ElementStore;
import org.openstreetmap.osmaxil.model.AbstractElement;
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
    
    @Autowired
    private LoadingStep loadingStep;
    
    @Autowired
    private ProcessingStep processingStep;
    
    @Autowired
    private SynchronizingStep synchronizingStep;
    
    @Override
    public void displayStats() {
        this.loadingStep.displayStats();
        this.processingStep.displayStats();
        this.synchronizingStep.displayStats();
        LOGGER_FOR_STATS.info("=== Advanced statistics ===");
        this.buildStatsByMatchingScore();
        this.displayStatsByMatchingScore();
        //showAllMatchingScores();
    }
    
    private void displayStatsByMatchingScore() {
        LOGGER_FOR_STATS.info("Repartition by matching scores:");
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(" - score between " + i * 10 + "% and " + (i + 1) * 10 + "% : ");
            sb.append(this.matchedElementsNbrByScore[i]);
            if (this.elementCache.getElements().size()  > 0) {
                sb.append(" (" + 100 * this.matchedElementsNbrByScore[i] / this.elementCache.getElements().size() + "%)");
            }
            sb.append(" elements <= " + this.alteredElementsNbrByScore[i] + " altered");
            sb.append(" (" + this.alterableElementsNbrByScore[i] + " were alterable)");
            LOGGER_FOR_STATS.info(sb);
        }
        LOGGER_FOR_STATS.info("Total of matched elements: " + this.matchedElementsNbr);
        LOGGER_FOR_STATS.info("Total of alterable elements: " + this.alterableElementsNbr);
        LOGGER_FOR_STATS.info("Total of altered elements: " + this.alteratedElementsNbr);
    }

    private void buildStatsByMatchingScore() {
        this.matchedElementsNbr = 0;
        this.alterableElementsNbr = 0;
        this.alteratedElementsNbr = 0;
        this.matchedElementsNbrByScore = new int[10];
        this.alteredElementsNbrByScore = new int[10];
        this.alterableElementsNbrByScore = new int[10];
        for (AbstractElement element : this.elementCache.getElements().values()) {
            Float score = element.getMatchingScore();
            if (score == null) {
                LOGGER_FOR_STATS.warn("Element " + element.getOsmId() + " doesn't have matching score !!");
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
                    LOGGER_FOR_STATS.error("Stats issue with element " + element.getOsmId() + ", its matching score is " + element.getMatchingScore());
                }
            }
        }
    }
    
    private void showAllMatchingScores() {
        LOGGER_FOR_STATS.info("Here are all matching scores:");
        for (AbstractElement element : elementCache.getElements().values()) {
            LOGGER_FOR_STATS.info("- score for element " + element.getOsmId() + " is " + element.getMatchingScore());
        }
    }

}