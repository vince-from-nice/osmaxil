package org.openstreetmap.osmaxil.plugin.common.scorer;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;

public abstract class AbstractMatchingScorer<ELEMENT extends AbstractElement> {

    public abstract float computeElementMatchingScore(ELEMENT element);

    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
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