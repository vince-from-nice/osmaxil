package org.openstreetmap.osmaxil.plugin.common.scorer;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.springframework.stereotype.Component;

@Component
public class ScoringStatsGenerator {
    
    private int matchedElementsNbr;

    //private int updatableElementsNbr;

    //private int updatedElementsNbr;

    private int[] matchedElementsNbrByScore;

    //private int[] updatableElementsNbrByScore;

    //private int[] updatedElementsNbrByScore;

    static protected final Logger LOGGER_FOR_STATS = Logger.getLogger("LoggerForStats");
    
    public void displayStatsByMatchingScore(Collection<AbstractElement> elements) {
        LOGGER_FOR_STATS.info("Repartions of elements by matching scores:");
        buildStatsByMatchingScore(elements);
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(" - score between " + i * 10 + "% and " + (i + 1) * 10 + "% : ");
            sb.append(this.matchedElementsNbrByScore[i]);
            if (elements.size() > 0) {
                sb.append(" (" + 100 * this.matchedElementsNbrByScore[i] / elements.size() + "%)");
            }
            sb.append(" elements");
//            sb.append(" <= " + this.updatedElementsNbrByScore[i] + " updated");
//            sb.append(" (" + this.updatableElementsNbrByScore[i] + " were updatable)");
            LOGGER_FOR_STATS.info(sb);
        }
        LOGGER_FOR_STATS.info("Total of matched elements: " + this.matchedElementsNbr);
//        LOGGER_FOR_STATS.info("Total of updatable elements: " + this.updatableElementsNbr);
//        LOGGER_FOR_STATS.info("Total of updated elements: " + this.updatedElementsNbr);
    }

    private void buildStatsByMatchingScore(Collection<AbstractElement> elements) {
        this.matchedElementsNbr = 0;
//        this.updatableElementsNbr = 0;
//        this.updatedElementsNbr = 0;
        this.matchedElementsNbrByScore = new int[10];
//        this.updatedElementsNbrByScore = new int[10];
//        this.updatableElementsNbrByScore = new int[10];
        for (AbstractElement element : elements) {
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
//                        if (element.isAltered()) {
//                            this.updatedElementsNbr++;
//                            this.updatedElementsNbrByScore[i]++;
//                        }
//                        if (this.isElementUpdatable(element)) {
//                            this.updatableElementsNbr++;
//                            this.updatableElementsNbrByScore[i]++;
//                        }
                        break;
                    }
                }
                if (!ok) {
                    LOGGER_FOR_STATS.error("Stats issue with element " + element.getOsmId()
                            + ", its matching score is " + element.getMatchingScore());
                }
            }
        }
    }

    /**
     *  Current implementation updates element if at least one of its tag is updatable, but plugins could overwrite
     */
//    private boolean isElementUpdatable(AbstractElement element) {
//        for (int j = 0; j < this.getUpdatableTagNames().length; j++) {
//            if (this.isElementTagUpdatable(element, this.getUpdatableTagNames()[j])) {
//                return true;
//            }
//        }
//        return false;
//    }

}
