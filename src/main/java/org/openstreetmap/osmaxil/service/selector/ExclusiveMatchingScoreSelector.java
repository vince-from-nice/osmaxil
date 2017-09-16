package org.openstreetmap.osmaxil.service.selector;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractUpdaterPlugin;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of AbstractMatchingScorer: 
 * It works exactly like the SimpleMatchingImportComparator except that elements with more than one matching import has the minimal global score.
 */
@Component
public class ExclusiveMatchingScoreSelector<Element extends AbstractElement> extends AbstractMatchingScoreSelector<Element> {
    
    @Override
    public float computeElementMatchingScore(AbstractElement element) {
        AbstractImport best = this.getBestMatchingImportByElement(element);
        if (best == null) {
            return AbstractUpdaterPlugin.MIN_MATCHING_SCORE;
        }
        float score = best.getMatchingScore();
        if (element.getMatchingImports().size() > 1) {
            // TEMP for having fun stats divide the score by the number of matching imports
            score = score / element.getMatchingImports().size();
            //return AbstractPlugin.MIN_MATCHING_SCORE;
        }
        return score;
    }

}
