package org.openstreetmap.osmaxil.dao;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GenericDemFile {
    
    @Value("${genericDemFile.srid}")
    private int srid;
    
    static private final Logger LOGGER = Logger.getLogger(Application.class);
    
}
