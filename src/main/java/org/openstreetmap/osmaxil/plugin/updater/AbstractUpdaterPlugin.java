package org.openstreetmap.osmaxil.plugin.updater;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;

public abstract class AbstractUpdaterPlugin<Element extends AbstractElement, Import extends AbstractImport>
        extends AbstractPlugin<Element, Import> {

    abstract public String[] getUpdatableTagNames();

    abstract public boolean isElementTagUpdatable(Element element, String tagName);

    abstract public boolean updateElementTag(Element element, String tagName);

}
