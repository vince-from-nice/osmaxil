package org.openstreetmap.osmium.plugin;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.AbstractImport;
import org.springframework.stereotype.Repository;

@Repository
public abstract class AbstractPlugin<Element extends AbstractElement, Import extends AbstractImport> implements
        Iterator<AbstractImport> {
    
    abstract public String getSourceLabel();

    abstract public Long[] findRelatedElementId(Import imp);

    abstract public Element createElement(long osmId);

    abstract protected boolean updateApiData(Import imp, Element element);

    abstract protected float computeMatchingScore(Import imp);

    static protected final Logger LOGGER = Logger.getLogger(Application.class);

    public boolean bindImportToElement(Element element, Import imp) {
        // Attach import to the element
        element.getMatchingImports().add(imp);
        imp.setElement(element); 
        StringBuilder sb = new StringBuilder("Matching imports is now : [ ");
        for (AbstractImport i : element.getMatchingImports()) {
            sb.append(i.getId() + " ");
        }
        LOGGER.info(sb.append("]").toString());
        // Compute matching score for the import
        imp.setMatchingScore(this.computeMatchingScore(imp));
        // Check if that import is the new winner
        boolean needToUpdate = false;
        AbstractImport best = element.getBestMatchingImport();
        sb = new StringBuilder("New import score is " + imp.getMatchingScore() + " and best matching import score is ");
        sb.append(best != null ? best.getMatchingScore() + " (id=" + best.getId() + ")" : "null");
        if (element.getBestMatchingImport() == null
                || element.getBestMatchingImport().getMatchingScore() < imp.getMatchingScore()) {
            sb.append(" => We have a new winner !!");
            element.setBestMatchingImport(imp);
            needToUpdate = updateApiData(imp, element);
        } else {
            sb.append(" => Loosing import");
        }
        LOGGER.info(sb.toString());
        if (needToUpdate) {
            LOGGER.info("Element need to be updated (" + ")");
        } else {
            LOGGER.info("Element doesn't need to be updated");
        }
        return needToUpdate;
    }
}
