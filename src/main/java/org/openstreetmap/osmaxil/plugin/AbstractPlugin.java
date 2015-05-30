package org.openstreetmap.osmaxil.plugin;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmPostgis;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractParser;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPlugin <Element extends AbstractElement, Import extends AbstractImport> {
    
    @Autowired
    protected OsmPostgis osmPostgis;
    
    abstract public List<MatchingElementId> findMatchingElements(Import imp);

    abstract public Element instanciateElement(long osmId);
    
    abstract public float computeImportMatchingScore(Import imp);
    
    abstract public float computeElementMatchingScore(Element element);
    
    abstract public float getMinimalMatchingScore();

    abstract public String getChangesetComment();
    
    abstract public String getChangesetSourceLabel();
    
    abstract public AbstractParser getParser();
    
    static public final float MIN_MATCHING_SCORE = 0.0f; 
    
    static public final float MAX_MATCHING_SCORE = 1.0f; 
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);

}
