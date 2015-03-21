package org.openstreetmap.osmium.plugin;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.AbstractImport;
import org.openstreetmap.osmium.data.RelevantElementId;
import org.openstreetmap.osmium.data.api.OsmApiRoot;
import org.openstreetmap.osmium.service.OsmApiService;
import org.openstreetmap.osmium.service.OsmPostgisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractPlugin<Element extends AbstractElement, Import extends AbstractImport> implements
        Iterator<AbstractImport> {
    
    @Autowired
    protected OsmPostgisService osmPostgisService;
    
    @Autowired
    protected OsmApiService osmApiService;
    
    abstract public String getChangesetSource();
    
    abstract public String getChangesetCommentl();

    abstract public List<RelevantElementId> findRelevantElements(Import imp);

    abstract public Element createElement(long osmId, long relationId, OsmApiRoot data);

    abstract public boolean updateApiData(Import imp, Element element);

    abstract public float computeMatchingScore(Import imp);

    static protected final Logger LOGGER = Logger.getLogger(Application.class);

}
