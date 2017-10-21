package org.openstreetmap.osmaxil.dao;

import java.util.List;

import org.openstreetmap.osmaxil.model.misc.Coordinates;

public interface ElevationDataSource {
	
	abstract void init(String source, int srid);
	
	abstract public int getSrid();
	
	abstract public double findElevationByCoordinates(double x, double y, int srid);
	
	abstract public List<Coordinates> findAllElevationsByGeometry(String includingGeomAsWKT, String excludingGeomAsWKT, int shrinkRadius, int geomSrid);

}
