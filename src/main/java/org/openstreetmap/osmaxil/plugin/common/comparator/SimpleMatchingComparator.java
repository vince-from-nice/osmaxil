package org.openstreetmap.osmaxil.plugin.common.comparator;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;

public class SimpleMatchingComparator extends AbstractMatchingComparator<AbstractElement>{

    @Override
    public float computeElementMatchingScore(AbstractElement element) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public AbstractImport getBestMatchingImportByElement(AbstractElement element) {
        // TODO Auto-generated method stub
        return null;
    }

}
