package org.openstreetmap.osmaxil.plugin.loader;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.ElevationDatabase;

public abstract class AbstractElevationDbLoader {

	protected ElevationDatabase database;
	
	static protected final Logger LOGGER = Logger.getLogger(Application.class);
	
	abstract public void loadData(String source);
}
