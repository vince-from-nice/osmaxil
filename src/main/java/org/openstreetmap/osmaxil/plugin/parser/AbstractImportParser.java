package org.openstreetmap.osmaxil.plugin.parser;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.springframework.stereotype.Repository;

@Repository
public abstract class AbstractImportParser implements Iterator<AbstractImport> {

    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    abstract public int getSrid();
    
}
