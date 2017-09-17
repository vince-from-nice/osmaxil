package org.openstreetmap.osmaxil.service.selector;

import org.openstreetmap.osmaxil.flow.AbstractUpdaterFlow;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.springframework.stereotype.Component;

/**
 * This class is the most simple implementation of AbstractMatchingScorer
 * - For the best matching import : it return the matching import with the best matching score
 * - For the element global score : it returns matching score of the best matching import
 */
@Component
public class SimpleMatchingScoreSelector extends AbstractMatchingScoreSelector<AbstractElement>{

    @Override
    public float computeElementMatchingScore(AbstractElement element) {
        AbstractImport best = this.getBestMatchingImportByElement(element);
        if (best == null) {
            return AbstractUpdaterFlow.MIN_MATCHING_SCORE;
        }
        return best.getMatchingScore();
    }

}
