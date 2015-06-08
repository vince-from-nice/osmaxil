package org.openstreetmap.osmaxil.plugin.updater;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;

public abstract class AbstractUpdaterPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
        extends AbstractPlugin<ELEMENT, IMPORT> {

    abstract public String[] getUpdatableTagNames();

    abstract public boolean isElementTagUpdatable(ELEMENT element, String tagName);

    abstract public boolean updateElementTag(ELEMENT element, String tagName);
    
    public boolean isElementAlterable(ELEMENT element) {
        for (int j = 0; j < this.getUpdatableTagNames().length; j++) {
            if(this.isElementTagUpdatable(element, this.getUpdatableTagNames()[j])) {
                return true;
            }
        }
        return false;
    }

}
