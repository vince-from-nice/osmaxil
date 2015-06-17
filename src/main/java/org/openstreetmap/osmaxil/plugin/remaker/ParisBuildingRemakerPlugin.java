package org.openstreetmap.osmaxil.plugin.remaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTagNames;
import org.openstreetmap.osmaxil.model.ElementType;
import org.openstreetmap.osmaxil.model.ElementWithParentFlags;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlMember;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlNd;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlNode;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRelation;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlTag;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlWay;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.BuildingMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.ParisBuildingParser;
import org.openstreetmap.osmaxil.plugin.common.scorer.AbstractMatchingScorer;
import org.openstreetmap.osmaxil.plugin.common.scorer.CumulativeOnAnyValueMatchingScorer;
import org.openstreetmap.osmaxil.util.IdIncrementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.vividsolutions.jts.geom.Point;

public class ParisBuildingRemakerPlugin extends AbstractRemakerPlugin<BuildingElement, BuildingImport> {

    @Autowired
    private ParisBuildingParser parser;

    @Autowired
    private BuildingMatcher matcher;
    
    @Autowired
    private CumulativeOnAnyValueMatchingScorer<BuildingElement> scorer;

    @Value("${plugins.parisBuildingMaker.minMatchingScore}")
    private float minMatchingScore;

    @Value("${plugins.parisBuildingMaker.changesetSourceLabel}")
    private String changesetSourceLabel;

    @Value("${plugins.parisBuildingMaker.changesetComment}")
    private String changesetComment;
    
    IdIncrementor idGenerator = new IdIncrementor(1);
    
    private Map<Long, OsmXmlRoot> newBuildingsByRemakableBuilding = new HashMap<>();

    private List<ElementWithParentFlags> oldNodesToDelete = new ArrayList<>();
    
    private Map<String, OsmXmlNode> newNodesByCoordinates = new HashMap<>();
    
    @Override
    public BuildingElement instanciateElement(long osmId) {
        return new BuildingElement(osmId);
    }

    @Override
    public boolean isElementAlterable(BuildingElement element) {
        // TODO Check if element has only common tags for the Cadastre import or if it's complex structure ?
        return true;
    }

    @Override
    public void prepareRemakingDataByElement(BuildingElement element) {
        LOGGER.debug("Building XML for remaking of element #" + element.getOsmId() + ":");
        this.remakableElements.add(element);
        this.newBuildingsByRemakableBuilding.put(element.getOsmId(), this.buildXmlForNewElementsCreation(element));
        this.oldNodesToDelete.addAll(this.buildElementToDelete(element));
        LOGGER.info("Remaking data has been prepared: remakableBuildings=" + this.remakableElements.size()
                + " newBuildings=" + this.newBuildingsByRemakableBuilding.size() + " newNodes=" + this.newNodesByCoordinates.size()
                + " oldNodes=" + this.oldNodesToDelete.size());
    }
    
    @Override
    public void finalizeRemakingData() {
        OsmXmlRoot root = new OsmXmlRoot();
        root.version = 0.6f;
        root.generator= Application.NAME;
        // Merge all new buildings (with their new nodes)
        for (Long id : this.newBuildingsByRemakableBuilding.keySet()) {
            OsmXmlRoot data = this.newBuildingsByRemakableBuilding.get(id);
            root.nodes.addAll(data.nodes);
            root.ways.addAll(data.ways);
            root.relations.addAll(data.relations);
        }
        // Merge deletions of all remakable buildings
//        for (BuildingElement element : this.remakableElements) {
//            OsmXmlWay way = new OsmXmlWay();
//            way.id = element.getOsmId();
//            way.action = "delete";
//            way.version = element.getApiData().ways.get(0).version;
//            way.changeset = element.getApiData().ways.get(0).changeset;
//            root.ways.add(way);
//        }
        // Merge deletions of nodes of all remakable buildings
//        for (ElementWithParentFlags e : this.oldNodesToDelete) {
//            // TODO Check if they can be deleted
//            if (true) {
//                OsmXmlNode node = new OsmXmlNode();
//                node.id = e.getOsmId();
//                node.action = "delete";
//                root.nodes.add(node);
//            }
//        }
        LOGGER.info("Remaking data has been finalized: nodes=" + root.nodes.size() + " ways=" + root.ways.size() + " relations=" + root.relations.size());
        this.remakingData = root;
    }
    
    private List<ElementWithParentFlags> buildElementToDelete(BuildingElement element) {
        ArrayList<ElementWithParentFlags> result = new ArrayList<>();
        for (OsmXmlNd nd : element.getApiData().ways.get(0).nds) {
            ElementWithParentFlags node = new ElementWithParentFlags();
            node.setOsmId(nd.ref);
            node.setType(ElementType.Node);
            // TODO use OverPass API to request all ways referencing current point ?
            List<Long> relatedWayIds = new ArrayList<>();
            for (Long relatedWayId: relatedWayIds) {
                ElementWithParentFlags.Parent parent = node.new Parent();
                parent.setOsmId(relatedWayId);
                parent.setFlag(false);
                node.getParents().add(parent);    
            }
            result.add(node);
        }
        return result;
    }
    
    private OsmXmlRoot buildXmlForNewElementsCreation(BuildingElement element) {
        OsmXmlRoot root = new OsmXmlRoot();
        
        
        // Create the relation
        OsmXmlRelation relation = new OsmXmlRelation();
        // Reuse all tags from original element
        relation.tags = element.getTags();
        // Set a negative ID based on the original element ID
        relation.id = -element.getOsmId();
        LOGGER.debug("\tBuilding new relation#"  + relation.id);
        // Add it into the root relation list
        root.relations.add(relation);
        
        // For each matching import:
        for (AbstractImport imp : element.getMatchingImports()) {
            BuildingImport bi = (BuildingImport) imp;
            
            // Create a new building part
            OsmXmlWay part = new OsmXmlWay();
            root.ways.add(part);
            // Set a negative ID based on the original element ID + index
            part.id = - this.idGenerator.getId();
            LOGGER.debug("\tBuilding part id=" + part.id);
            part.visible = "true";
            // Add the building:part tag
            OsmXmlTag tag = new OsmXmlTag();
            tag.k = "building:part";
            tag.v = "yes";
            part.tags.add(tag);
            // Add the building:level tag
            tag = new OsmXmlTag();
            tag.k = ElementTagNames.BUILDING_LEVELS;
            Integer levels = bi.getLevels() + 1; // US way of levels counting
            tag.v = levels.toString();
            part.tags.add(tag);
            
            // Add member into the relation
            OsmXmlMember member = new OsmXmlMember();
            member.ref = part.id;
            member.role = "part";
            member.type = "way";
            relation.members.add(member);
                        
            // Extract points from the geometry
//            List<Coordinates> points = computeBuildingPartGeometry(bi);
//            // Reparse transformed geometry to build a list of points
//            wktConverted = wktConverted.replace("POLYGON((", "").replace("))", "");
//            coords = wktConverted.split(",");
//            for (int i = 0; i < coords.length; i++) {
//                String[] p = coords[i].trim().split(" ");
//                Coordinates point = new Coordinates(p[0], p[1], "");
//                result.add(point);
//            }
            
            long firstNodeId = 0;
            // For each point except for the last one
            for (int i = 0; i < bi.getPoints().size() - 1; i++) {
                Point point = bi.getPoints().get(i);
                
                // Try to get it from the internal cache
                String key = point.getX() + "," + point.getY();
                OsmXmlNode node = this.newNodesByCoordinates.get(key);
                if (node == null) {
                    // Create a new node (into the root)
                    node = new OsmXmlNode();
                    node.id = - this.idGenerator.getId();
                    node.visible = "true";
                    node.lon = Double.toString(point.getX());
                    node.lat = Double.toString(point.getY());
                    root.nodes.add(node);
                    this.newNodesByCoordinates.put(key, node);
                    LOGGER.debug("\t\tPoint id=" + node.id + " x=" + point.getX() + " y=" + point.getY());
                }
                
                // Create new node reference (into the building part)
                OsmXmlNd nd = new OsmXmlNd();
                nd.ref = node.id;
                part.nds.add(nd);
                // Keep id of the first node
                if (firstNodeId == 0) {
                    firstNodeId = node.id;
                }
            }
            
//            long firstNodeId = 0;
//            // For each point except for the last one
//            for (int i = 0; i < points.size() - 1; i++) {
//                Coordinates point = points.get(i);
//                
//                // Try to get it from the internal cache
//                String key = point.x + "," + point.y;
//                OsmXmlNode node = this.newNodesByCoordinates.get(key);
//                if (node == null) {
//                    // Create a new node (into the root)
//                    node = new OsmXmlNode();
//                    node.id = - this.idGenerator.getId();
//                    node.visible = "true";
//                    node.lon = point.x;
//                    node.lat = point.y;
//                    root.nodes.add(node);
//                    this.newNodesByCoordinates.put(key, node);
//                    LOGGER.debug("\t\tPoint id=" + node.id + " x=" + point.x + " y=" + point.y);
//                }
//                
//                // Create new node reference (into the building part)
//                OsmXmlNd nd = new OsmXmlNd();
//                nd.ref = node.id;
//                part.nds.add(nd);
//                // Keep id of the first node
//                if (firstNodeId == 0) {
//                    firstNodeId = node.id;
//                }
//            }
            
            // Don't forget to close the way with the first node
            OsmXmlNd nd = new OsmXmlNd();
            nd.ref = firstNodeId;
            part.nds.add(nd);
        }
        
        return root;
    }

    @Override
    public ParisBuildingParser getParser() {
        return parser;
    }
    
    @Override
    public AbstractMatcher<BuildingImport> getMatcher() {
        return this.matcher;
    }

    @Override
    public AbstractMatchingScorer<BuildingElement> getScorer() {
      return this.scorer;
    }

    @Override
    public float getMinimalMatchingScore() {
        return minMatchingScore;
    }

    @Override
    public String getChangesetSourceLabel() {
        return changesetSourceLabel;
    }

    @Override
    public String getChangesetComment() {
        return changesetComment;
    }

}
