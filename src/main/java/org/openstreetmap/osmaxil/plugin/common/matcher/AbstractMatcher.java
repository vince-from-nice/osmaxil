package org.openstreetmap.osmaxil.plugin.common.matcher;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmPostgis;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractMatcher {

    @Autowired
    protected OsmPostgis osmPostgisService;
    
    abstract public List<MatchingElementId> findMatchingImport(BuildingImport imp, int srid);
    
    abstract public float computeMatchingScore(BuildingImport imp);
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
}
