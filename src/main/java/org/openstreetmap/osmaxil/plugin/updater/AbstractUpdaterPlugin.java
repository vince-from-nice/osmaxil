package org.openstreetmap.osmaxil.plugin.updater;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractUpdaterPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
        extends AbstractPlugin<ELEMENT, IMPORT> {

    abstract public String[] getUpdatableTagNames();

    protected Map<String, Integer> counterMap = new HashMap<String, Integer>();
    
    abstract public boolean isElementTagUpdatable(ELEMENT element, String tagName);

    abstract public boolean updateElementTag(ELEMENT element, String tagName);
    
    @PostConstruct
    public void init() {
        for (String  updatableTagName : this.getUpdatableTagNames()) {
            this.counterMap.put(updatableTagName, 0);
        }
    }
    
    public boolean isElementAlterable(ELEMENT element) {
        // Current implementation updates element if at least one of its tag is updatable, but plugins could overwrite that...
        for (int j = 0; j < this.getUpdatableTagNames().length; j++) {
            if(this.isElementTagUpdatable(element, this.getUpdatableTagNames()[j])) {
                return true;
            }
        }
        return false;
    }
    
    public  void displaySynchronizingStatistics(){
        for (String  updatableTagName : this.getUpdatableTagNames()) {
            LOGGER_FOR_STATS.info(" - number of updates on the tag " + updatableTagName + ": " + this.counterMap.get(updatableTagName));    
        }
        
    }

}
