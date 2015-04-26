package org.openstreetmap.osmaxil.service;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractElementUpdaterPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsGenerator extends AbstractService {

    private int matchedElementsNbr;

    private int updatableElementsNbr;

    private int updatedElementsNbr;

    private int[] matchedElementsNbrByScore;

    private int[] updatableElementsNbrByScore;

    private int[] updatedElementsNbrByScore;

    @Autowired
    private ElementCache elementCache;

    public void generateStats() {
        LOGGER.info("=== Statistics ===");
        if (this.plugin instanceof AbstractElementUpdaterPlugin) {
            generateUpdatingStats();
        } else if (this.plugin instanceof AbstractElementUpdaterPlugin) {
            generateMakingStats();
        } 
    }

    private void generateUpdatingStats() {
        // Old basic matching method
        LOGGER.info("*** Statistics with the old matching method ***");
        this.buildUpdatingStatsWithBestMatchingImports();
        displayUpdatingStats();
        // New extended matching method
        LOGGER.info("*** Statistics with the new matching method ***");
        for (String updatableTagName : ((AbstractElementUpdaterPlugin) this.plugin).getUpdatableTagNames()) {
            LOGGER.info("* Statistics for the updatable tag " + updatableTagName);
            this.buildUpdatingStatsWithBestAccumulatedImports(updatableTagName);
            displayUpdatingStats();
        }
    }
    
    private void generateMakingStats() {
    }
    
    private void displayUpdatingStats() {
        LOGGER.info("Number of matched elements: " + this.matchedElementsNbr);
        LOGGER.info("Number of updatable elements: " + this.updatableElementsNbr);
        LOGGER.info("Number of updated elements: " + this.updatedElementsNbr);
        LOGGER.info("Repartition by matching scores:");
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("- between " + i * 10 + "% and " + (i + 1) * 10 + "% : ");
            sb.append(this.matchedElementsNbrByScore[i]);
            if (this.elementCache.getElements().size()  > 0) {
                sb.append(" (" + 100 * this.matchedElementsNbrByScore[i] / this.elementCache.getElements().size() + "%)");
            }
            sb.append(" elements including " + this.updatedElementsNbrByScore[i] + " that have been updated");
            sb.append(" (" + this.updatableElementsNbrByScore[i] + " were updatable)");
            LOGGER.info(sb);
        }
    }

    @Obsolete
    private void buildUpdatingStatsWithBestMatchingImports() {
        this.matchedElementsNbr = 0;
        this.updatableElementsNbr = 0;
        this.updatedElementsNbr = 0;
        this.matchedElementsNbrByScore = new int[10];
        this.updatedElementsNbrByScore = new int[10];
        this.updatableElementsNbrByScore = new int[10];
        for (AbstractElement element : this.elementCache.getElements().values()) {
            AbstractImport best = element.getBestMatchingImport();
            if (best == null) {
                LOGGER.warn("Element " + element.getOsmId() + " doesn't have any best matching import !!");
            } else {
                for (int i = 0; i < 10; i++) {
                    if (best.getMatchingScore() <= (i + 1) * 0.1) {
                        this.matchedElementsNbr++;
                        this.matchedElementsNbrByScore[i]++;
                        if (element.isUpdated()) {
                            this.updatedElementsNbr++;
                            this.updatedElementsNbrByScore[i]++;
                        }
                        boolean updatable = false;
                        for (String tagName : ((AbstractElementUpdaterPlugin) this.plugin).getUpdatableTagNames()) {
                            if (((AbstractElementUpdaterPlugin) this.plugin).isElementTagUpdatable(element, tagName)) {
                                updatable = true;
                            }
                        }
                        if (updatable) {
                            this.updatableElementsNbr++;
                            this.updatableElementsNbrByScore[i]++;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void buildUpdatingStatsWithBestAccumulatedImports(String updatableTagName) {
        this.matchedElementsNbr = 0;
        this.updatableElementsNbr = 0;
        this.updatedElementsNbr = 0;
        this.matchedElementsNbrByScore = new int[10];
        this.updatedElementsNbrByScore = new int[10];
        this.updatableElementsNbrByScore = new int[10];
        for (AbstractElement element : this.elementCache.getElements().values()) {
            Float bestTotalScore = element.getBestTotalScoreByTagName(updatableTagName);
            if (bestTotalScore == null) {
                LOGGER.warn("Element " + element.getOsmId() + " doesn't have any best total matching score !!");
            } else {
                for (int i = 0; i < 10; i++) {
                    if (bestTotalScore <= (i + 1) * 0.1) {
                        this.matchedElementsNbr++;
                        this.matchedElementsNbrByScore[i]++;
                        if (element.isUpdated()) {
                            this.updatedElementsNbr++;
                            this.updatedElementsNbrByScore[i]++;
                        }
                        if (((AbstractElementUpdaterPlugin) this.plugin).isElementTagUpdatable(element, updatableTagName)) {
                            this.updatableElementsNbr++;
                            this.updatableElementsNbrByScore[i]++;
                        }
                        break;
                    }
                }
            }
        }
    }

}