package org.openstreetmap.osmaxil.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.misc.Coordinates;

public interface ElevationDataSource {
	
	static public final Logger LOGGER = Logger.getLogger(Application.class);
	
	abstract void init(String source, int srid);
	
	abstract public int getSrid();
	
	abstract public double findElevationByCoordinates(double x, double y, int srid);
	
	abstract public List<Coordinates> findAllElevationsByGeometry(String includingGeomAsWKT, String excludingGeomAsWKT, int shrinkRadius, int geomSrid);

}
