package org.openstreetmap.osmaxil.plugin;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmPostgis;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractParser;
import org.openstreetmap.osmaxil.plugin.common.scorer.AbstractMatchingScorer;
import org.openstreetmap.osmaxil.step.StatisticsStep;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPlugin <ELEMENT extends AbstractElement, IMPORT extends AbstractImport> {
    
    @Autowired
    protected OsmPostgis osmPostgis;
    
    abstract public ELEMENT instanciateElement(long osmId);
        
    abstract public float getMinimalMatchingScore();

    abstract public String getChangesetComment();
    
    abstract public String getChangesetSourceLabel();
    
    abstract public AbstractParser<IMPORT> getParser();
    
    abstract public AbstractMatcher<IMPORT> getMatcher();
    
    abstract public AbstractMatchingScorer<ELEMENT> getScorer();
    
    //abstract public List<MatchingElementId> findMatchingElements(IMPORT imp);
    
    //abstract public float computeImportMatchingScore(IMPORT imp);
    
    //abstract public float computeElementMatchingScore(ELEMENT element);
    
    abstract public boolean isElementAlterable(ELEMENT element);
    
    abstract public void displayProcessingStatistics();
    
    abstract public void displaySynchronizingStatistics();
    
    static public final float MIN_MATCHING_SCORE = 0.0f; 
    
    static public final float MAX_MATCHING_SCORE = 1.0f; 
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    static protected final Logger LOGGER_FOR_STATS = Logger.getLogger(StatisticsStep.class);
    
    public float computeElementMatchingScore(ELEMENT element) {
        return this.getScorer().computeElementMatchingScore(element);
    }
    
    public List<MatchingElementId> findMatchingElements(IMPORT imp) {
        return this.getMatcher().findMatchingImport(imp, this.getParser().getSrid());
    }

    public float computeImportMatchingScore(IMPORT imp) {
        return this.getMatcher().computeMatchingImportScore(imp);
    }

}
