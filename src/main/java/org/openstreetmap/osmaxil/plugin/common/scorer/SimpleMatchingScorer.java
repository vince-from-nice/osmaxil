package org.openstreetmap.osmaxil.plugin.common.scorer;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.stereotype.Component;

/**
 * This class is the most simple implementation of AbstractMatchingScorer
 * - For the best matching import : it return the matching import with the best matching score
 * - For the element global score : it returns matching score of the best matching import
 */
@Component
public class SimpleMatchingScorer extends AbstractMatchingScorer<AbstractElement>{

    @Override
    public float computeElementMatchingScore(AbstractElement element) {
        AbstractImport best = this.getBestMatchingImportByElement(element);
        if (best == null) {
            return AbstractPlugin.MIN_MATCHING_SCORE;
        }
        return best.getMatchingScore();
    }

}
