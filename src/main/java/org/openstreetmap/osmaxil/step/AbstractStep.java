package org.openstreetmap.osmaxil.step;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmApi;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractStep {
    
    protected AbstractPlugin plugin;
    
    @Autowired
    protected OsmApi osmApiService;
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);

    static protected final String LOG_SEPARATOR = "==========================================================";
    
    public AbstractPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(AbstractPlugin plugin) {
        this.plugin = plugin;
    }
}
