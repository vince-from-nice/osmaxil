package org.openstreetmap.osmium.service;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.data.AbstractElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsGenerator {

    private int[] elementsNbrByScoreRange;

    @Autowired
    private ElementCache elementCache;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    public void displayStats() {
        LOGGER.info("=== Statistics ===");
        diplayStatsForScoreRanges();
    }

    private void diplayStatsForScoreRanges() {
        LOGGER.info("Here is the repartition of best matching scores :");
        makeStatsForScoreRanges();
        for (int i = 0; i < 10; i++) {
            LOGGER.info("- between " + i * 10 + "% and " + (i + 1) * 10 + "% : " + this.elementsNbrByScoreRange[i] + " ("
                    + 100 * this.elementsNbrByScoreRange[i] / this.elementCache.getElements().size() + "%)");
        }
    }

    private void makeStatsForScoreRanges() {
        this.elementsNbrByScoreRange = new int[10];
        for (AbstractElement element : this.elementCache.getElements().values()) {
            for (int i = 0; i < 10; i++) {
                if (element.getBestMatchingImport().getMatchingScore() < i * 0.1) {
                    this.elementsNbrByScoreRange[i]++;
                    break;
                }
            }
        }
    }
}