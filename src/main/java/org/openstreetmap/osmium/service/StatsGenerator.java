package org.openstreetmap.osmium.service;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class StatsGenerator {

    private int[] matchedElementsNbrByScore;
    
    private int[] updatableElementsNbrByScore;

    @Autowired
    private ElementCache elementCache;
    
    @Autowired
    @Qualifier (value="OpenDataParisCsvPlugin")
    private AbstractPlugin plugin;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    public void makeStats() {
        LOGGER.info("=== Statistics ===");
        this.generateStats();
        displayStats();
    }

    private void displayStats() {
        LOGGER.info("Number of matched elements : " + this.elementCache.getElements().size());
        LOGGER.info("Element repartition by best matching scores :");
        for (int i = 0; i < 10; i++) {
            LOGGER.info("- between " + i * 10 + "% and " + (i + 1) * 10 + "% : " + this.matchedElementsNbrByScore[i]
                    + " (" + 100 * this.matchedElementsNbrByScore[i] / this.elementCache.getElements().size()
                    + "%) elements including " + this.updatableElementsNbrByScore[i] + " that are updatable");
        }
    }

    private void generateStats() {
        this.matchedElementsNbrByScore = new int[10];
        this.updatableElementsNbrByScore = new int[10];
        int badElementsNbrByScore = 0;
        for (AbstractElement element : this.elementCache.getElements().values()) {
            boolean ranged = false;
            for (int i = 0; i < 10; i++) {
                if (element.getBestMatchingImport().getMatchingScore() < (i + 1) * 0.1) {
                    this.matchedElementsNbrByScore[i]++;
                    if (this.plugin.isElementUpdatable(element.getBestMatchingImport(), element)) {
                        this.updatableElementsNbrByScore[i]++;
                    }
                    ranged = true;
                    break;
                }
            }
            if (!ranged) {
                badElementsNbrByScore++;
            }
        }
        if (badElementsNbrByScore > 0) {
            LOGGER.warn("Number of bad elements (matching score > 1.0) : " + badElementsNbrByScore);
        }
    }
}