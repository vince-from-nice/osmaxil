package org.openstreetmap.osmaxil.plugin;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.OsmPostgis;
import org.openstreetmap.osmaxil.dao.OsmStandardApi;
import org.openstreetmap.osmaxil.dao.OsmXmlFile;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public abstract class AbstractPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport> {

    // =========================================================================
    // Instance variables
    // =========================================================================

    protected List<IMPORT> loadedImports = new ArrayList<>();

    private Geometry includingArea;

    private Geometry excludingArea;

    @Value("${osmaxil.filteringArea.including}")
    protected String includingAreaString;

    @Value("${osmaxil.filteringArea.excluding}")
    protected String excludingAreaString;

    @Value("${osmaxil.syncMode}")
    protected String synchronizationMode;

    @Autowired
    protected OsmPostgis osmPostgis;

    @Autowired
    protected OsmStandardApi osmStandardApi;

    @Autowired
    protected OsmXmlFile osmXmlFile;

    protected long counterForParsedImports;

    protected long counterForFilteredImports;

    protected long counterForLoadedImports;

    protected GeometryFactory geometryFactory;

    // =========================================================================
    // Static variables
    // =========================================================================

    static protected final Logger LOGGER = Logger.getLogger(Application.class);

    static protected final Logger LOGGER_FOR_STATS = Logger.getLogger("LoggerForStats");

    static protected final String LOG_SEPARATOR = "==========================================================";

    // =========================================================================
    // Abstract methods
    // =========================================================================

    abstract public void process();

    abstract public void synchronize();

    abstract protected AbstractParser<IMPORT> getParser();

    abstract protected String getChangesetComment();

    abstract protected String getChangesetSourceLabel();

    abstract public void displayProcessingStatistics();

    abstract public void displaySynchronizingStatistics();

    // =========================================================================
    // Public methods
    // =========================================================================

    public void prepare() {
    	LOGGER.info("There is nothing to prepare for that plugin.");
	}
    
    public void load() {
        while (this.getParser().hasNext()) {
            try {
                IMPORT imp = this.getParser().next();
                if (imp == null) {
                    LOGGER.warn("Import is null, skipping it...");
                    break;
                }
                this.counterForParsedImports++;
                LOGGER.info("Loading import #" + this.counterForParsedImports + ": " + imp);
                // Check if the import coordinates are fine with the bounding boxes
                if (!this.checkCoordinatesWithFilteringArea(imp.getLongitude(), imp.getLatitude())) {
                    this.counterForFilteredImports++;
                    LOGGER.warn("Import has coordinates which are outside the filtering area, skipping it...");
                } else {
                    this.loadedImports.add(imp);
                    this.counterForLoadedImports++;
                }
            } catch (java.lang.Exception e) {
                LOGGER.error("Unable to load an import: ", e);
            } finally {
                LOGGER.info(LOG_SEPARATOR);
            }
        }
    }

    public void displayLoadingStatistics() {
        LOGGER_FOR_STATS.info("=== Loading statistics ===");
        LOGGER_FOR_STATS.info("Total of parsed imports: " + this.counterForParsedImports);
        LOGGER_FOR_STATS.info("Total of filtered out imports: " + this.counterForFilteredImports);
        LOGGER_FOR_STATS.info("Total of loaded imports: " + this.counterForLoadedImports);
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

    private boolean checkCoordinatesWithFilteringArea(double x, double y) {
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

}
