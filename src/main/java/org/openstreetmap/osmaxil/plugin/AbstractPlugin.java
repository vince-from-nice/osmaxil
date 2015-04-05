package org.openstreetmap.osmaxil.plugin;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.data.AbstractElement;
import org.openstreetmap.osmaxil.data.AbstractImport;
import org.openstreetmap.osmaxil.data.MatchingElementId;
import org.openstreetmap.osmaxil.data.api.OsmApiRoot;
import org.openstreetmap.osmaxil.service.OsmApiService;
import org.openstreetmap.osmaxil.service.OsmPostgisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractPlugin<Element extends AbstractElement, Import extends AbstractImport> implements
        Iterator<AbstractImport> {
    
    @Autowired
    protected OsmPostgisService osmPostgisService;
    
    @Autowired
    protected OsmApiService osmApiService;
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    abstract public String[] getUpdatableTagNames();
    
    abstract public float getMinMatchingScoreForUpdate();
    
    abstract public String getChangesetSourceLabel();
    
    abstract public String getChangesetComment();

    abstract public List<MatchingElementId> findMatchingElements(Import imp);

    abstract public Element createElement(long osmId, long relationId, OsmApiRoot data);

    abstract public boolean isElementTagUpdatable(Element element, String tagName);
    
    abstract public boolean updateElementTag(Element element, String tagName);

    abstract public float computeImportMatchingScore(Import imp);
    
}