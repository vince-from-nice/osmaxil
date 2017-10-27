package org.openstreetmap.osmaxil.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.ElevationImport;

public interface ElevationDataSource {
	
	public enum Type {DB, FILE};
	
	public enum Use {DTM, DSM};
	
	static public final Logger LOGGER = Logger.getLogger(Application.class);
	
	abstract void init(String source, int srid);
	
	abstract public int getSrid();
	
	abstract public ElevationImport findElevationByCoordinates(double x, double y, float valueScale, int srid);
	
	abstract public List<ElevationImport> findAllElevationsByGeometry(String includingGeomAsWKT, String excludingGeomAsWKT, float valueScale, int shrinkRadius, int geomSrid);

}
