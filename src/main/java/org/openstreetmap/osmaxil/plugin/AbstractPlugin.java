package org.openstreetmap.osmaxil.plugin;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmApiDAO;
import org.openstreetmap.osmaxil.dao.OsmPostgisDAO;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPlugin <Element extends AbstractElement, Import extends AbstractImport> implements
Iterator<AbstractImport> {
    
    @Autowired
    protected OsmPostgisDAO osmPostgisService;
    
    @Autowired
    protected OsmApiDAO osmApiService;
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    abstract public String getChangesetSourceLabel();
    
    abstract public String getChangesetComment();

    abstract public List<MatchingElementId> findMatchingElements(Import imp);

    abstract public Element createElementInCache(long osmId, long relationId, OsmApiRoot data);

    abstract public float computeMatchingScore(Import imp);

}
