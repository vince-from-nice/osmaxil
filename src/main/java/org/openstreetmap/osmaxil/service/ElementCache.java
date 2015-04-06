package org.openstreetmap.osmaxil.service;

import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.Exception;
import org.openstreetmap.osmaxil.data.AbstractElement;
import org.openstreetmap.osmaxil.data.AbstractImport;
import org.openstreetmap.osmaxil.data.MatchingElementId;
import org.openstreetmap.osmaxil.data.api.OsmApiRoot;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ElementCache {

    private Map<Long, AbstractElement> elements;
    
    @Autowired
    @Qualifier (value="OpenDataParisBuildingPlugin")
    private AbstractPlugin pluginAutowiredBySpring;
 
    //@Autowired (value="OpenDataParisBuildingPlugin")
    private AbstractPlugin<AbstractElement, AbstractImport> plugin;

    @Autowired
    private OsmApiService osmApiService;

    static private final Logger LOGGER = Logger.getLogger(Application.class);
    
    public ElementCache() throws java.lang.Exception {
        this.elements = new Hashtable<Long, AbstractElement>();
    }
    
    @PostConstruct
    public void init() {
        //TODO Autowire specialized plugin
        this.plugin = this.pluginAutowiredBySpring;
        this.osmApiService.init(this.plugin);
    }
    
    public AbstractElement getOrCreateElement(MatchingElementId relevantElementId) throws Exception {
        long osmId  = relevantElementId.getOsmId();
        AbstractElement element = this.elements.get(osmId);
        if (element == null) {
            // Fetch data from OSM API
            OsmApiRoot apiData = this.osmApiService.readElement(osmId);
            if (apiData == null) {
                throw new Exception("Unable to fetch data from OSM API for element#" + osmId);
            }
            element = (AbstractElement) this.plugin.createElement(osmId, relevantElementId.getRelationId(), apiData);
            this.elements.put(osmId, element);
        }/* else {
            // If element was already present refresh its data
            element.setApiData(apiData);                
        }*/
        return element;
    }

    public Map<Long, AbstractElement> getElements() {
        return elements;
    }
}
