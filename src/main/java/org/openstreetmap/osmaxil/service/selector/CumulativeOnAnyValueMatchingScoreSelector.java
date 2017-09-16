package org.openstreetmap.osmaxil.service.selector;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractUpdaterPlugin;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of AbstractMatchingScorer :
 * - For the best matching import : it return the matching import with the best matching score
 * - For the element global score : it accumulates matching scores of all matching score of the element.
 */
@Component
public class CumulativeOnAnyValueMatchingScoreSelector<Element extends AbstractElement> extends AbstractMatchingScoreSelector<Element> {
    
    @Override
    public float computeElementMatchingScore(AbstractElement element) {
        float score = AbstractUpdaterPlugin.MIN_MATCHING_SCORE;
        for (AbstractImport imp : element.getMatchingImports()) {
            score += imp.getMatchingScore();
        }
        if (score > AbstractUpdaterPlugin.MAX_MATCHING_SCORE) {
            score = AbstractUpdaterPlugin.MAX_MATCHING_SCORE;
        }
        return score;
    }
    
}
