package org.openstreetmap.osmium.plugin.building;

import org.openstreetmap.osmium.data.BuildingElement;
import org.openstreetmap.osmium.data.BuildingImport;
import org.openstreetmap.osmium.data.api.OsmApiRoot;
import org.openstreetmap.osmium.plugin.AbstractPlugin;
import org.openstreetmap.osmium.service.OsmPostgisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public abstract class AbstractBuildingPlugin extends AbstractPlugin<BuildingElement, BuildingImport> {

    @Autowired
    private OsmPostgisService osmPostgisService;
    
    @Override
    public String getChangesetCommentl() {
        return "Updating building heights and levels";
    }
    
    @Override
    public BuildingElement createElement(long osmId, OsmApiRoot data) {
        BuildingElement element = new BuildingElement(osmId);
        element.setApiData(data);
        // Set original values
        element.setOriginalHeight(element.getHeight());
        element.setOriginalLevels(element.getLevels());
        return element;
    }

    @Override
    public Long[] findRelatedElementId(BuildingImport imp) {
        Long[] result = new Long[0];
        BuildingImport building = (BuildingImport) imp;
        if (building.getLat() != null && building.getLon() != null) {
            result = this.findBuildingIDsByLatLon(building.getLon(), building.getLat());
        } else if (building.getGeometry() != null) {
            result = this.findBuildingIDsByGeometry(building.getGeometry());
            LOGGER.info("OSM IDs of buildings matching (" + building.getGeometry() + ") : ");
        } else {
            LOGGER.error("Unable to find building because there's no coordinates neither geometry");
        }
        StringBuffer sb = new StringBuffer("OSM IDs of matching buildings : [ ");
        for (Long id : result) {
            sb.append(id + " ");
        }
        LOGGER.info(sb.toString() + "]");
        return result;
    }
    
    @Override
    protected boolean updateApiData(BuildingImport imp, BuildingElement element) {
        boolean needToUpdate = false;
        // Update tags only if original values don't exist
        if (element.getOriginalLevels() == null && imp.getLevels() != null) {
            LOGGER.info("===> Updating levels to " + imp.getLevels());
            element.setLevels(imp.getLevels());
            needToUpdate = true;
        }
        if (element.getOriginalHeight() == null && imp.getHeight() != null) {
            LOGGER.info("===> Updating height to $building.height");
            element.setHeight(imp.getHeight());
            needToUpdate = true;
        }
        return needToUpdate;
    }
    
    @Override
    protected float computeMatchingScore(BuildingImport imp) {
        if (imp.getArea() == null) {
            LOGGER.warn("Unable to compute score because import has NO area");
            return 0f;
        }
        if (imp.getElement() == null) {
            LOGGER.warn("Unable to compute score because import has NO element attached");
            return 0f;
        }
        // Compare area between import and element
        int elementArea = this.osmPostgisService.getElementAreaById(imp.getElement().getOsmId());
        // TODO cache it for next imports 
        LOGGER.info("Element computed area is [" + elementArea + "]");
        if (elementArea > 0) {
         // returns a float which tends to 1.0 when area tends are going closer (and tends to 0.0 when different)
            if ( imp.getArea() < elementArea) {
                return ((float) imp.getArea() / elementArea);
            } else {
                return ((float)  elementArea / imp.getArea());
            }
        }
        // TODO Add other criteria such the centrality of the import coords into element area
        return 0f;
    }
    
    private Long[] findBuildingIDsByGeometry(String geometry) {
        LOGGER.info("Looking in PostGIS for buildings containing GEOMETRY=" + geometry + ":");
        // TODO Test PostGIS request with intersection operator
        String query = "select osm_id from planet_osm_polygon "
                + "where building <> '' and  ST_Intersects(way, ST_GeomFromText('" + geometry + "', 4326));";
        return this.osmPostgisService.findElementIdsByQuery(query);
    }

    private Long[]  findBuildingIDsByLatLon(double lon, double lat) {
        //List<Long> result = new ArrayList<Long>();
        LOGGER.info("Looking in PostGIS for buildings containing POINT(" + lon + ", " + lat + "):");
        String query = "select osm_id from planet_osm_polygon "
                + "where building <> '' and  ST_Contains(way, ST_Transform(ST_GeomFromText('POINT(" + lon + " " + lat
                + ")', 4326), 900913));";
        return this.osmPostgisService.findElementIdsByQuery(query);
    }
        
}
