package org.openstreetmap.osmaxil.plugin.common.matcher;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmPostgis;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.misc.MatchingElementId;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractMatcher<IMPORT extends AbstractImport> {

    @Autowired
    protected OsmPostgis osmPostgisService;
    
    abstract public List<MatchingElementId> findMatchingImport(IMPORT imp, int srid);
    
    abstract public float computeMatchingImportScore(IMPORT imp);
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
}
