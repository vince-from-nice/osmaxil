package org.openstreetmap.osmaxil.step;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openstreetmap.osmaxil.Exception;
import org.openstreetmap.osmaxil.dao.ElementStore;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiRoot;
import org.openstreetmap.osmaxil.plugin.updater.AbstractUpdaterPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@Service
public class LoadingStep  extends AbstractStep {

    private long counterForLoadedImports;
    
    private long counterForFilterededImports;
    
    private long counterForMatchedImports;
    
    @Autowired
    private ElementStore elementCache;
    
    private GeometryFactory geometryFactory;
    
    private Geometry includingArea;
    
    private Geometry excludingArea;
    
    @Value("${osmaxil.filteringArea.including}")
    private String includingAreaString;
    
    @Value("${osmaxil.filteringArea.excluding}")
    private String excludingAreaString;
    
    @PostConstruct
    private void init() throws ParseException {
        this.geometryFactory = new GeometryFactory();
        WKTReader wktReader = new WKTReader();
        // Build the including and excluding area
        this.includingArea = wktReader.read(this.includingAreaString);
        this.excludingArea = wktReader.read(this.excludingAreaString);
    }
        
    @PreDestroy
    public void close() {
        LOGGER.info("=== Closing import org.openstreetmap.osmaxil.plugin.loader ===");
        LOGGER.info("Total of loaded imports: " + this.counterForLoadedImports);
        LOGGER.info("Total of filtered imports: " + this.counterForFilterededImports);
        LOGGER.info("Total of matched imports: " + this.counterForMatchedImports);
    }

    public void loadImports() {
        LOGGER.info("=== Loading imports ===");
        LOGGER.info(LOG_SEPARATOR);
        while (this.plugin.getParser().hasNext()) {
            try {
                    AbstractImport imp = (AbstractImport) this.plugin.getParser().next();
                    this.counterForLoadedImports++;
                    this.loadImport(imp);
            } catch (java.lang.Exception e) {
                LOGGER.error("An import has failed: ", e);
            } finally {
                LOGGER.info(LOG_SEPARATOR);
            }
        }
    }
 
    private void loadImport(AbstractImport imp) {
        if (imp == null) {
            LOGGER.warn("Import is null, skipping it...");
            return;
        }
        LOGGER.info("Loading import #" + this.counterForLoadedImports + ": " +  imp);
        // Check if the import coordinates are fine with the bounding boxes
        if (!this.checkCoordinatesWithFilteringArea(imp.getLon(), imp.getLat())) {
            this.counterForFilterededImports++;
            LOGGER.warn("Import has invalid coordinates, skipping it...");
            return;
        }
        // Find relevant element
        List<MatchingElementId> relevantElementIds = this.plugin.findMatchingElements(imp);
        if (relevantElementIds.size() > 0) {
            this.counterForMatchedImports++;
        }
        // For each matching elements
        for (MatchingElementId relevantElementId : relevantElementIds) {
            long osmId = relevantElementId.getOsmId();
            // Skip negative IDs (ie. multipolygon relations whose outer member has not been found)
            if (osmId < 0) {
                break;
            }
            // Get related element from the cache or create it
            AbstractElement element = null;
            try {
                element = this.getOrCreateElement(relevantElementId);
            } catch (Exception e) {
                LOGGER.error("Skipping element id=" + osmId + " (" + e.getMessage() + ")");
                break;
            }
            LOGGER.info(element);
            // And bind the import to it
            this.bindImportToElement(element, imp);
        }
    }
    
    private boolean checkCoordinatesWithFilteringArea(double  x, double y) {
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
    
    private AbstractElement getOrCreateElement(MatchingElementId relevantElementId) throws Exception {
        long osmId  = relevantElementId.getOsmId();
        AbstractElement element = this.elementCache.getElements().get(osmId);
        if (element == null) {
            // Fetch data from OSM API
            OsmApiRoot apiData = this.osmApiService.readElement(osmId);
            if (apiData == null) {
                throw new Exception("Unable to fetch data from OSM API for element#" + osmId);
            }
            // Instanciate a new element
            element = this.plugin.instanciateElement(osmId);
            element.setRelationId(relevantElementId.getRelationId());
            element.setApiData(apiData);
            this.elementCache.getElements().put(osmId, element);
            // Need to do additional stuff if the current plugin is a updater
            if (this.plugin instanceof AbstractUpdaterPlugin) {
                for (String tagName : ((AbstractUpdaterPlugin) this.plugin).getUpdatableTagNames()) {
                    element.getOriginalValuesByTagNames().put(tagName, element.getTagValue(tagName));
                }
            }
        } /*else {
            // If element was already present refresh its data
            element.setApiData(apiData);                
        }*/
        return element;
    }
    
    private void bindImportToElement(AbstractElement element, AbstractImport imp) {
        // Attach import to the element
        element.getMatchingImports().add(imp);
        imp.setElement(element); 
        StringBuilder sb = new StringBuilder("Matching imports are now : [ ");
        for (AbstractImport i : element.getMatchingImports()) {
            sb.append(i.getId() + " ");
        }
        LOGGER.info(sb.append("]").toString());
    }
    
}
