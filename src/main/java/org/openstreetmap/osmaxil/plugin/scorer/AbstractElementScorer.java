package org.openstreetmap.osmaxil.plugin.scorer;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.ElevationRasterFile;
import org.openstreetmap.osmaxil.dao.OsmPostgisDB;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.springframework.beans.factory.annotation.Autowired;

abstract public class AbstractElementScorer<ELEMENT extends AbstractElement> {
	
    @Autowired
    protected OsmPostgisDB osmPostgis;
    
	@Autowired
	protected ElevationRasterFile genericRasterFile;
	
	static protected final Logger LOGGER = Logger.getLogger(Application.class);

	abstract public float computeElementMatchingScore(ELEMENT element, float minMatchingScore);

}