package org.openstreetmap.osmium.service;

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
public class StatsGenerator {

    private int matchedElementsNbr;

    private int updatableElementsNbr;

    private int updatedElementsNbr;

    private int[] matchedElementsNbrByScore;

    private int[] updatableElementsNbrByScore;

    private int[] updatedElementsNbrByScore;

    @Autowired
    private ElementCache elementCache;

    @Autowired
    @Qualifier (value="OpenDataParisCsvPlugin")
    private AbstractPlugin pluginAutowiredBySpring;
 
    //@Autowired (value="OpenDataParisCsvPlugin")
    private AbstractPlugin<AbstractElement, AbstractImport> plugin;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    @PostConstruct
    public void init() {
        this.plugin = this.pluginAutowiredBySpring;
    }
    
    public void generateStats() {
        LOGGER.info("=== Statistics ===");
        // Old matching method
        LOGGER.info("*** Statistics with the old matching method ***");
        this.buildStatsWithBestMatchingImports();
        displayStats();
        // New matching method
        LOGGER.info("*** Statistics with the new matching method ***");
        for (String updatableTagName : this.plugin.getUpdatableTagNames()) {
            LOGGER.info("* Statistics for the updatable tag " + updatableTagName);
            this.buildStatsWithBestAccumulatedImports(updatableTagName);
            displayStats();
        }
    }

    private void displayStats() {
        LOGGER.info("Number of matched elements: " + this.matchedElementsNbr);
        LOGGER.info("Number of updatable elements: " + this.updatableElementsNbr);
        LOGGER.info("Number of updated elements: " + this.updatedElementsNbr);
        LOGGER.info("Repartition by matching scores:");
        for (int i = 0; i < 10; i++) {
            LOGGER.info("- between " + i * 10 + "% and " + (i + 1) * 10 + "% : " + this.matchedElementsNbrByScore[i]
                    + " (" + 100 * this.matchedElementsNbrByScore[i] / this.elementCache.getElements().size()
                    + "%) elements including " + this.updatedElementsNbrByScore[i] + " that have been updated ("
                    + this.updatableElementsNbrByScore[i] + " were updatable)");
        }
    }

    private void buildStatsWithBestMatchingImports() {
        this.matchedElementsNbr = 0;
        this.matchedElementsNbrByScore = new int[10];
        this.updatedElementsNbrByScore = new int[10];
        this.updatableElementsNbrByScore = new int[10];
        for (AbstractElement element : this.elementCache.getElements().values()) {
            for (int i = 0; i < 10; i++) {
                if (element.getBestMatchingImport().getMatchingScore() <= (i + 1) * 0.1) {
                    this.matchedElementsNbr++;
                    this.matchedElementsNbrByScore[i]++;
                    if (element.isUpdated()) {
                        this.updatedElementsNbr++;
                        this.updatedElementsNbrByScore[i]++;
                    }
                    if (this.plugin.isElementUpdatable(element.getBestMatchingImport(), element)) {
                        this.updatableElementsNbr++;
                        this.updatableElementsNbrByScore[i]++;
                    }
                    break;
                }
            }
        }
    }
    
    private void buildStatsWithBestAccumulatedImports(String updatableTagName) {
        this.matchedElementsNbr = 0;
        this.matchedElementsNbrByScore = new int[10];
        this.updatedElementsNbrByScore = new int[10];
        this.updatableElementsNbrByScore = new int[10];
        for (AbstractElement element : this.elementCache.getElements().values()) {
            for (int i = 0; i < 10; i++) {
                if (element.getBestTotalScoreByTagName(updatableTagName) <= (i + 1) * 0.1) {
                    this.matchedElementsNbr++;
                    this.matchedElementsNbrByScore[i]++;
                    if (element.isUpdated()) {
                        this.updatedElementsNbr++;
                        this.updatedElementsNbrByScore[i]++;
                    }
                    if (this.plugin.isElementUpdatable(element.getBestMatchingImport(), element)) {
                        this.updatableElementsNbr++;
                        this.updatableElementsNbrByScore[i]++;
                    }
                    break;
                }
            }
        }
    }
    
}