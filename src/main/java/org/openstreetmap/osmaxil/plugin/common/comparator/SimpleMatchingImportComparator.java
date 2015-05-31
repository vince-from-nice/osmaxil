package org.openstreetmap.osmaxil.plugin.common.comparator;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.stereotype.Component;

/**
 * This class is the most simple implementation of AbstractMatchingComparator: for each element it
 * just considers the matching imports which has the best matching score.
 */
@Component
public class SimpleMatchingImportComparator extends AbstractMatchingImportComparator<AbstractElement>{

    @Override
    public float computeElementMatchingScore(AbstractElement element) {
        AbstractImport best = this.getBestMatchingImportByElement(element);
        if (best == null) {
            return AbstractPlugin.MIN_MATCHING_SCORE;
        }
        return best.getMatchingScore();
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
