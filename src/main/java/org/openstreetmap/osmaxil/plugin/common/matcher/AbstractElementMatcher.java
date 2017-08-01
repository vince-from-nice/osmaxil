package org.openstreetmap.osmaxil.plugin.common.matcher;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmPostgisDB;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.misc.MatchingElementId;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractElementMatcher<ELEMENT extends AbstractElement> {

    @Autowired
    protected OsmPostgisDB osmPostgis;
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    abstract public List<MatchingElementId> findMatchingImports(ELEMENT element, int srid);
    
    public abstract float computeMatchingElementScore(ELEMENT element);
    
}
