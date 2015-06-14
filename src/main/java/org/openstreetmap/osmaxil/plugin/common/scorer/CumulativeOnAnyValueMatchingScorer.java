package org.openstreetmap.osmaxil.plugin.common.scorer;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of AbstractMatchingScorer :
 * - For the best matching import : it return the matching import with the best matching score
 * - For the element global score : it accumulates matching scores of all matching score of the element.
 */
@Component
public class CumulativeOnAnyValueMatchingScorer<Element extends AbstractElement> extends AbstractMatchingScorer<Element> {
    
    @Override
    public float computeElementMatchingScore(AbstractElement element) {
        float score = AbstractPlugin.MIN_MATCHING_SCORE;
        for (AbstractImport imp : element.getMatchingImports()) {
            score += imp.getMatchingScore();
        }
        if (score > AbstractPlugin.MAX_MATCHING_SCORE) {
            score = AbstractPlugin.MAX_MATCHING_SCORE;
        }
        return score;
    }
    
}
