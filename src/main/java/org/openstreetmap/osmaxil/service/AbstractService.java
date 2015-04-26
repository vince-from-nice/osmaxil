package org.openstreetmap.osmaxil.service;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmApiDAO;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractService {
    
    protected AbstractPlugin plugin;
    
    @Autowired
    protected OsmApiDAO osmApiService;
    
    static protected final Logger LOGGER = Logger.getLogger(Application.class);

    static protected final String LOG_SEPARATOR = "==========================================================";

    
    public AbstractPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(AbstractPlugin plugin) {
        this.plugin = plugin;
    }
}
