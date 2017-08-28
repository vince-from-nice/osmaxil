package org.openstreetmap.osmaxil.service.parser;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.springframework.stereotype.Repository;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

@Repository
public abstract class AbstractImportParser<IMPORT extends AbstractImport> implements Iterator<IMPORT> {
    
    protected GeometryFactory geometryFactory =  new GeometryFactory();
    
    protected WKTReader wktReader = new WKTReader();

    static protected final Logger LOGGER = Logger.getLogger(Application.class);
    
    abstract public int getSrid();
    
}
