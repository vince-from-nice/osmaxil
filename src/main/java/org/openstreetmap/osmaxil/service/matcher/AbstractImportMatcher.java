package org.openstreetmap.osmaxil.service.matcher;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmPostgisDB;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.misc.MatchingElementId;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractImportMatcher<IMPORT extends AbstractImport> {

    @Autowired
    protected OsmPostgisDB osmPostgis;
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    abstract public List<MatchingElementId> findMatchingElements(IMPORT imp, int srid);
    
    public abstract float computeMatchingImportScore(IMPORT imp);
    
}
