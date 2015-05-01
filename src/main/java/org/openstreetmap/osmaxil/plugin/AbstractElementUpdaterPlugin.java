package org.openstreetmap.osmaxil.plugin;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;

public abstract class AbstractElementUpdaterPlugin<Element extends AbstractElement, Import extends AbstractImport>
        extends AbstractPlugin<Element, Import> {

    abstract public Element instanciateElement(long osmId);

    abstract public String[] getUpdatableTagNames();

    abstract public float getMinMatchingScoreForUpdate();

    abstract public boolean isElementTagUpdatable(Element element, String tagName);

    abstract public boolean updateElementTag(Element element, String tagName);

    public Element instanciateElement(long osmId, long relationId, OsmApiRoot data) {
        Element element = instanciateElement(osmId);
        element.setRelationId(relationId);
        element.setApiData(data);
        for (String tagName : this.getUpdatableTagNames()) {
            element.getOriginalValuesByTagNames().put(tagName, element.getTagValue(tagName));
        }
        return element;
    }

}
