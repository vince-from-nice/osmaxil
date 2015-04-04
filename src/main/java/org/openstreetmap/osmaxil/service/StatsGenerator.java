package org.openstreetmap.osmaxil.service;

import javax.annotation.PostConstruct;

import org.apache.http.annotation.Obsolete;
import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.data.AbstractElement;
import org.openstreetmap.osmaxil.data.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
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
    @Qualifier(value = "OpenDataParisCsvPlugin")
    private AbstractPlugin pluginAutowiredBySpring;

    // @Autowired (value="OpenDataParisCsvPlugin")
    private AbstractPlugin<AbstractElement, AbstractImport> plugin;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    @PostConstruct
    public void init() {
        this.plugin = this.pluginAutowiredBySpring;
    }

    public void generateStats() {
        LOGGER.info("=== Statistics ===");
        // Old basic matching method
        LOGGER.info("*** Statistics with the basic matching method ***");
        this.buildStatsWithBestMatchingImports();
        displayStats();
        // New extended matching method
        LOGGER.info("*** Statistics with the extended matching method ***");
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
            StringBuilder sb = new StringBuilder();
            sb.append("- between " + i * 10 + "% and " + (i + 1) * 10 + "% : ");
            sb.append(this.matchedElementsNbrByScore[i]);
            if (this.elementCache.getElements().size()  > 0) {
                sb.append(" (" + 100 * this.matchedElementsNbrByScore[i] / this.elementCache.getElements().size() + "%) ");
            }
            sb.append("elements including " + this.updatedElementsNbrByScore[i] + " that have been updated");
            sb.append(" (" + this.updatableElementsNbrByScore[i] + " were updatable)");
            LOGGER.info(sb);
        }
    }

    @Obsolete
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
                    boolean updatable = false;
                    for (String tagName : this.plugin.getUpdatableTagNames()) {
                        if (this.plugin.isElementTagUpdatable(element, tagName)) {
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

    private void buildStatsWithBestAccumulatedImports(String updatableTagName) {
        this.matchedElementsNbr = 0;
        this.updatableElementsNbr = 0;
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
                    if (this.plugin.isElementTagUpdatable(element, updatableTagName)) {
                        this.updatableElementsNbr++;
                        this.updatableElementsNbrByScore[i]++;
                    }
                    break;
                }
            }
        }
    }

}