package org.openstreetmap.osmaxil.flow;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.Exception;
import org.openstreetmap.osmaxil.dao.OsmPostgisDB;
import org.openstreetmap.osmaxil.dao.OsmStandardApi;
import org.openstreetmap.osmaxil.dao.OsmXmlFile;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public abstract class __AbstractImportFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport> {

	// =========================================================================
	// Instance variables
	// =========================================================================

	protected List<IMPORT> loadedImports = new ArrayList<>();

	private Geometry includingArea;

	private Geometry excludingArea;

	protected GeometryFactory geometryFactory;

	@Value("${osmaxil.syncMode}")
	protected String synchronizationMode;

	@Value("${osmaxil.minMatchingScore}")
	protected float minMatchingScore;
	
	@Value("${osmaxil.refCodeSuffix}")
	protected String refCodeSuffix;
	
	@Value("${osmaxil.useRefCode}")
	protected boolean useReferenceCode;
	
	@Value("${osmaxil.filteringArea.srid}")
	protected int filteringAreaSrid;

	@Value("${osmaxil.filteringArea.including}")
	protected String includingAreaString;

	@Value("${osmaxil.filteringArea.excluding}")
	protected String excludingAreaString;

	@Autowired
	protected ApplicationContext appContext;

	@Autowired
	protected OsmPostgisDB osmPostgis;

	@Autowired
	protected OsmStandardApi osmStandardApi;

	@Autowired
	protected OsmXmlFile osmXmlFile;

	// =========================================================================
	// Static variables
	// =========================================================================

	static protected final Logger LOGGER = Logger.getLogger(Application.class);

	static protected final Logger LOGGER_FOR_STATS = Logger.getLogger("LoggerForStats");

	static protected final String LOG_SEPARATOR = "==========================================================";

	// =========================================================================
	// Abstract methods
	// =========================================================================

	abstract public void load() throws Exception;

	abstract public void process();

	abstract public void synchronize();

	abstract public void displayLoadingStatistics();

	abstract public void displayProcessingStatistics();

	abstract public void displaySynchronizingStatistics();

	// =========================================================================
	// Public and protected methods
	// =========================================================================

	protected boolean checkCoordinatesWithFilteringArea(double x, double y) {
		Geometry geom = this.geometryFactory.createPoint(new Coordinate(x, y));
		IntersectionMatrix includingMatrix = geom.relate(this.includingArea);
		if (!includingMatrix.isWithin()) {
			LOGGER.info("Coordinates (" + x + ", " + y + ") are outside the including area " + this.includingAreaString);
			return false;
		}
		IntersectionMatrix excludingMatrix = geom.relate(this.excludingArea);
		if (excludingMatrix.isWithin()) {
			LOGGER.info("Coordinates (" + x + ", " + y + ") are inside the excluding area " + this.excludingAreaString);
			return false;
		}
		return true;
	}

	// =========================================================================
	// Private methods
	// =========================================================================

	@PostConstruct
	private void init() throws ParseException {
		this.geometryFactory = new GeometryFactory();
		WKTReader wktReader = new WKTReader();
		// Build the including and excluding area
		this.includingArea = wktReader.read(this.includingAreaString);
		this.excludingArea = wktReader.read(this.excludingAreaString);
	}

}
