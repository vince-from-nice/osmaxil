package org.openstreetmap.osmaxil.plugin.common.comparator;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.AbstractElement;

abstract class AbstractComparator<Element extends AbstractElement> {

    public abstract float computeElementMatchingScore(Element element, String matchingTagName);

    public abstract String getBestTagValueByElement(long osmId);
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);

}