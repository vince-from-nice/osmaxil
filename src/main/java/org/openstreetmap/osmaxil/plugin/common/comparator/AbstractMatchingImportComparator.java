package org.openstreetmap.osmaxil.plugin.common.comparator;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;

abstract class AbstractMatchingImportComparator<Element extends AbstractElement> {

    public abstract float computeElementMatchingScore(Element element);

    public abstract AbstractImport getBestMatchingImportByElement(Element element);
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);

}