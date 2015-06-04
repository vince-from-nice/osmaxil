package org.openstreetmap.osmaxil.plugin.common.matcher;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.springframework.stereotype.Component;

@Component
public class BuildingMatcher extends AbstractMatcher {
 
   @Override
    public List<MatchingElementId> findMatchingImport(BuildingImport imp, int srid) {
        List<MatchingElementId> result = new ArrayList<MatchingElementId>();
        Long[] ids = new Long[0];
        // Find in PostGIS all buildings matching (ie. containing) the import
        BuildingImport building = (BuildingImport) imp;
        if (building.getLat() != null && building.getLon() != null) {
            ids = this.findBuildingIDsByLatLon(building.getLon(), building.getLat(), srid);
        } else if (building.getGeometry() != null) {
            ids = this.findBuildingIDsByGeometry(building.getGeometry(), srid);
            LOGGER.info("OSM IDs of buildings matching (" + building.getGeometry() + ") : ");
        } else {
            LOGGER.error("Unable to find building because there's no coordinates neither geometry");
        }
        // Parsing the IDs to check if they refers to normal elements (ie. ways) or relations
        StringBuffer sb = new StringBuffer("OSM IDs of matching buildings : [ ");
        for (int i = 0; i < ids.length; i++) {
            MatchingElementId relevantElement = new MatchingElementId();
            // If ID is positive it means it's a normal element (ie. a way)
            if (ids[i] > 0) {
                relevantElement.setOsmId(ids[i]);
                relevantElement.setRelationId(-1);
            } 
            // If ID is negative it means it's a multipolygon relations => need to find its relevant outer member
            else {
                LOGGER.debug("A multipolygon relation has been found (" + ids[i] + "), looking for its relevant outer member");
                relevantElement.setOsmId(this.findRelevantOuterMemberId(- ids[i], imp));
                relevantElement.setRelationId(- ids[i]);
            }
            result.add(relevantElement);
            sb.append(ids[i] + " ");
        }
        LOGGER.info(sb.toString() + "]");
        return result;
    }
    
   @Override
    public float computeMatchingScore(BuildingImport imp) {
        BuildingElement element = (BuildingElement) imp.getElement();
        float result = 0f;
        if (imp.getArea() == null) {
            LOGGER.warn("Unable to compute score because building import has NO area");
            return 0f;
        }
        if (imp.getElement() == null) {
            LOGGER.warn("Unable to compute score because building import has NO element attached");
            return 0f;
        }
        // Get element area
        int elementArea = element.getComputedArea();
        // If not yet computed do it and store the result for further matching imports
        if (elementArea == 0) {
            elementArea = this.computeBuildingArea(element);
            element.setComputedArea(elementArea);
        }
        // Compare area between import and element
        if (elementArea > 0) {
         // Returns a float which tends to 1.0 when areas are going closer (and tends to 0.0 if different)
            if ( imp.getArea() < elementArea) {
                result = ((float) imp.getArea() / elementArea);
            } else {
                result = ((float)  elementArea / imp.getArea());
            }
        }
        // TODO Add other criteria such the "centrality" of the import into the element area
        return result;
    }
    
//    public BuildingPart createBuildingPart(BuildingImport imp) {
//        BuildingPart bp = new BuildingPart();
//        OsmApiRoot root = new OsmApiRoot();
//        bp.setApiData(root);
//        // TODO create nodes
//        return bp;
//    }
//    
//    public BuildingRelation createBuildingRelation(BuildingElement element) {
//        BuildingRelation br = new BuildingRelation();
//        OsmApiRoot root = new OsmApiRoot();
//        br.setApiData(root);
//        OsmApiRelation relation = new OsmApiRelation();
//        // Reuse tags from orignal element
//        relation.tags = element.getTags();
//        root.relations.add(relation);
//        // Set a negative ID based on the original element ID
//        br.getApiData().ways.get(0).id = - element.getOsmId();
//        return br;
//    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private methods 
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private int computeBuildingArea(BuildingElement element) {
        // If the related element belongs to a relation, consider it instead of the element itself (osm2pgsql doesn't store relation members) 
        long elementId = element.getOsmId();
        if (element.getRelationId() > 0) {
            elementId = - element.getRelationId(); // reinverse the ID because osm2pgsql stores relations like that
        }
        // TODO use JTS instead of PostGIS ?
        int elementArea = this.osmPostgisService.getPolygonAreaById(elementId);
        // TODO cache it for next imports 
        LOGGER.info("OSM building " + element.getOsmId() + " area has been computed: " + elementArea);
        return elementArea;
    }
    
    private long findRelevantOuterMemberId(long relationId, BuildingImport imp) {
        long result = 0;
        // Fetch members from PostGIS
        String membersString = this.osmPostgisService.getRelationMembers(relationId);
        // Parse members strings
        membersString = membersString.substring(1, membersString.length() - 1);
        String[] members = membersString.split(","); 
        List<Long> outerMemberIds = new ArrayList<Long>();
        for (int i = 0; i < members.length; i++) {
            if ("outer".equals(members[i]) && members[i-1].startsWith("w")) {
                outerMemberIds.add(Long.parseLong(members[i - 1].substring(1)));
                //OsmApiRoot memberData = this.osmApiService.readElement(outerMemberId);
            }
        }
        // For now support only relation with just one outer member
        if (outerMemberIds.size() == 1) {
            result = outerMemberIds.get(0);
            LOGGER.info("For multipolygon relation with id=" + relationId + ", outer member with id=[" + result +"] has been found");
        } 
        // Else we just return the negative relation ID
        else {
            result = - relationId;
            LOGGER.warn("For multipolygon relation with id=" + relationId + ", no outer member has been found (members are " + membersString + ")");
        }
        return result;
    }
    
    private Long[] findBuildingIDsByGeometry(String geometry, int srid) {
        LOGGER.info("Looking in PostGIS for buildings containing GEOMETRY=" + geometry + ":");
        // TODO Test PostGIS request with intersection operator
        String query = "select osm_id from planet_osm_polygon "
                + "where building <> '' and  ST_Intersects(way, ST_GeomFromText('" + geometry + "', 4326));";
        return this.osmPostgisService.findElementIdsByQuery(query);
    }

    private Long[]  findBuildingIDsByLatLon(double lon, double lat, int srid) {
        //List<Long> result = new ArrayList<Long>();
        LOGGER.info("Looking in PostGIS for buildings containing POINT(" + lon + ", " + lat + "):");
        String query = "select osm_id from planet_osm_polygon "
                + "where building <> '' and  ST_Contains(way, ST_Transform(ST_GeomFromText('POINT(" + lon + " " + lat
                + ")', " + srid + "), " + this.osmPostgisService.getSrid() + "));";
        return this.osmPostgisService.findElementIdsByQuery(query);
    }
        
}
