package org.openstreetmap.osmaxil.dao;

import java.util.Hashtable;
import java.util.Map;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.springframework.stereotype.Service;

/*
 * For now it's just an Hashtable but it should be a persistable store in the future
 */
@Service
public class ElementStore {
    
    private Map<Long, AbstractElement> elements;
    
    public ElementStore() throws java.lang.Exception {
        this.elements = new Hashtable<Long, AbstractElement>();
    }
    
    public Map<Long, AbstractElement> getElements() {
        return elements;
    }
    
}
