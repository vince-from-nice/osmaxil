package org.openstreetmap.osmaxil.plugin;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;

public abstract class AbstractUpdaterPlugin<Element extends AbstractElement, Import extends AbstractImport>
        extends AbstractPlugin<Element, Import> {

    abstract public String[] getUpdatableTagNames();

    abstract public boolean isElementTagUpdatable(Element element, String tagName);

    abstract public boolean updateElementTag(Element element, String tagName);

}
