package org.openstreetmap.osmium.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.AbstractImport;
import org.openstreetmap.osmium.data.MatchingElementId;
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
    
    protected List<String> updatableTagNames = new ArrayList<String>();
    
    public List<String> getUpdatableTagNames() {
        return updatableTagNames;
    }
    
    abstract public String getChangesetSource();
    
    abstract public String getChangesetCommentl();

    abstract public List<MatchingElementId> findMatchingElements(Import imp);

    abstract public Element createElement(long osmId, long relationId, OsmApiRoot data);

    abstract public boolean isElementUpdatable(Import imp, Element element);
    
    abstract public boolean updateElementData(Import imp, Element element);

    abstract public float computeImportMatchingScore(Import imp);
    
    abstract public float getMinMatchingScoreForUpdate();
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);

}
