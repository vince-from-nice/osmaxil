package org.openstreetmap.osmaxil.plugin.common.comparator;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of AbstractMatchingComparator: 
 * It works exactly like the SimpleMatchingImportComparator but elements with more than one matching import has the minimal global score.
 */
@Component
public class ExclusiveMatchingImportComparator<Element extends AbstractElement> extends AbstractMatchingImportComparator<Element> {
    
    @Override
    public float computeElementMatchingScore(AbstractElement element) {
        AbstractImport best = this.getBestMatchingImportByElement(element);
        if (best == null) {
            return AbstractPlugin.MIN_MATCHING_SCORE;
        }
        float score = best.getMatchingScore();
        if (element.getMatchingImports().size() > 1) {
            // TEMP for having fun stats divide the score by the number of matching imports
            score = score / element.getMatchingImports().size();
            //return AbstractPlugin.MIN_MATCHING_SCORE;
        }
        return score;
    }

    @Override
    public AbstractImport getBestMatchingImportByElement(AbstractElement element) {
        AbstractImport best = null;
        for (AbstractImport imp : element.getMatchingImports()) {
            if (best == null || best.getMatchingScore() < imp.getMatchingScore()) {
                best = imp;
            }
        }
        return best;
    }

}
