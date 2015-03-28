package org.openstreetmap.osmium.service;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsGenerator {
    
    @Autowired
    private ElementCache elementCache;

    static private final Logger LOGGER = Logger.getLogger(Application.class);
    
    public void displayStats() {
        LOGGER.info("=== Statistics ===");
        LOGGER.info("TODO");
    }
}
