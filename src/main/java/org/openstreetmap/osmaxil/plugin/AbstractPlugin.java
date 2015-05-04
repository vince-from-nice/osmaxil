package org.openstreetmap.osmaxil.plugin;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmPostgis;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;
import org.openstreetmap.osmaxil.plugin.loader.AbstractImportLoader;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPlugin <Element extends AbstractElement, Import extends AbstractImport> {
    
    @Autowired
    protected OsmPostgis osmPostgis;
    
    abstract public List<MatchingElementId> findMatchingElements(Import imp);

    abstract public Element instanciateElement(long osmId);
    
    abstract public float computeMatchingScore(Import imp);
    
    abstract public float getMinMatchingScore();

    abstract public String getChangesetComment();
    
    abstract public String getChangesetSourceLabel();
    
    abstract public AbstractImportLoader getLoader();
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    public Element instanciateElement(long osmId, long relationId, OsmApiRoot data) {
        Element element = instanciateElement(osmId);
        element.setRelationId(relationId);
        element.setApiData(data);
        return element;
    }

}
