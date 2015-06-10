package org.openstreetmap.osmaxil.plugin.remaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.Coordinates;
import org.openstreetmap.osmaxil.model.ElementType;
import org.openstreetmap.osmaxil.model.ElementWithParentFlags;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiMember;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiNd;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiNode;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiRelation;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiRoot;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiTag;
import org.openstreetmap.osmaxil.model.xml.osm.OsmApiWay;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.BuildingMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.ParisBuildingParser;
import org.openstreetmap.osmaxil.plugin.common.scorer.AbstractMatchingScorer;
import org.openstreetmap.osmaxil.plugin.common.scorer.CumulativeOnAnyValueMatchingScorer;
import org.openstreetmap.osmaxil.util.IdIncrementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
    
    protected Map<Long, OsmApiRoot> newBuildingsByRemakableBuilding = new HashMap<Long, OsmApiRoot>();

    protected List<ElementWithParentFlags> oldNodesToDelete = new ArrayList<>();
    
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
                + " newBuildings=" + this.newBuildingsByRemakableBuilding.size() + " oldNodes="
                + this.oldNodesToDelete.size());
    }
    
    @Override
    public void finalizeRemakingData() {
        OsmApiRoot root = new OsmApiRoot();
        // Merge all new buildings (with their new nodes)
        for (Long id : this.newBuildingsByRemakableBuilding.keySet()) {
            OsmApiRoot data = this.newBuildingsByRemakableBuilding.get(id);
            root.nodes.addAll(data.nodes);
            root.ways.addAll(data.ways);
            root.relations.addAll(data.relations);
        }
        // Merge deletions of all remakable buildings
        for (BuildingElement element : this.remakableElements) {
            OsmApiWay way = new OsmApiWay();
            way.id = element.getOsmId();
            way.action = "delete";
            root.ways.add(way);
        }
        // Merge deletions of nodes of all remakable buildings
        for (ElementWithParentFlags e : this.oldNodesToDelete) {
            // TODO Check if they can be deleted
            if (true) {
                OsmApiNode node = new OsmApiNode();
                node.id = e.getOsmId();
                node.action = "delete";
                root.nodes.add(node);
            }
        }
        LOGGER.info("Remaking data has been finalized: nodes=" + root.nodes.size() + " ways=" + root.ways.size() + " relations=" + root.relations.size());
        this.remakingData = root;
    }
    
    private List<ElementWithParentFlags> buildElementToDelete(BuildingElement element) {
        ArrayList<ElementWithParentFlags> result = new ArrayList<>();
        for (OsmApiNode n : element.getApiData().nodes) {
            ElementWithParentFlags node = new ElementWithParentFlags();
            node.setOsmId(element.getOsmId());
            node.setType(ElementType.Node);
            // TODO use OverPass API to request all ways referencing current point ?
            List<Long> relatedWayIds = null;
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
    
    private OsmApiRoot buildXmlForNewElementsCreation(BuildingElement element) {
        OsmApiRoot root = new OsmApiRoot();
        IdIncrementor idGen = new IdIncrementor(1);
        
        // Instanciate sublists
//        root.relations = new ArrayList<OsmApiRelation>();
//        root.nodes = new ArrayList<OsmApiNode>();
//        root.ways = new ArrayList<OsmApiWay>();
        
        // Create relation
        OsmApiRelation relation = new OsmApiRelation();
        // Reuse all tags from original element
        relation.tags = element.getTags();
        // Set a negative ID based on the original element ID
        relation.id = -element.getOsmId();
        // Instanciate sublists
        //relation.members = new ArrayList<OsmApiMember>();
        // Add it into the root relation list
        root.relations.add(relation);
        
        // For each matching import:
        for (AbstractImport imp : element.getMatchingImports()) {
            BuildingImport bi = (BuildingImport) imp;
            
            // Create a new building part
            OsmApiWay part = new OsmApiWay();
            root.ways.add(part);
            // Set a negative ID based on the original element ID + index
            part.id = - idGen.getId();
            LOGGER.debug("\tBuilding part id=" + part.id);
            // Instanciate sublists
            //part.nds = new ArrayList<OsmApiNd>();
            //part.tags = new ArrayList<OsmApiTag>();
            // Add the building:part tag
            OsmApiTag tag = new OsmApiTag();
            tag.k = "building:part";
            tag.v = "yes";
            part.tags.add(tag);
            // Add the building:level tag
            tag = new OsmApiTag();
            tag.k = "building:level";
            Integer levels = bi.getLevels() + 1; // US way of levels counting
            tag.v = levels.toString();
            part.tags.add(tag);
            
            // Add member into the relation
            OsmApiMember member = new OsmApiMember();
            member.ref = part.id;
            member.role = "part";
            member.type = "way";
            relation.members.add(member);
                        
            // For each point of the import:
            List<Coordinates> points = computeBuildingPartGeometry(bi);
            for (Coordinates point : points) {
                // Create a new node (into the root)
                OsmApiNode node = new OsmApiNode();
                node.id = - idGen.getId();
                node.lon = point.x;
                node.lat = point.y;
                root.nodes.add(node);
                LOGGER.debug("\t\tPoint id=" + node.id + " x=" + point.x + " y=" + point.y);
                
                // Create new node reference (into the building part)
                OsmApiNd nd = new OsmApiNd();
                nd.ref = node.id;
                part.nds.add(nd);
            }
        }
        
        return root;
    }

    // TODO Eventually move that method into ParisData CSV loader
    private List<Coordinates> computeBuildingPartGeometry(BuildingImport imp) {
        List<Coordinates> result = new ArrayList<Coordinates>();
        StringBuilder wkt = new StringBuilder("POLYGON(("); 
        String geom = imp.getGeometry();
        // Convert ODP format into WKT
        geom = geom.replace("[", "").replace("]", "").replace(",", "");
        String[] coords = geom.split(" ");
        for (int i = 0; i < coords.length; i++) {
            wkt.append(coords[i]);
            if (i % 2 == 1 && i < coords.length - 1) {
                wkt.append(", ");
            } else {
                wkt.append(" ");
            }
        }
        wkt.append("))");
        // Convert geometry coordinates to OSM SRID
        int srid = ((ParisBuildingParser) this.getParser()).getSrid();
        String wktConverted = this.osmPostgis.tranformGeometry(wkt.toString(), srid);
        // Reparse transformed geometry to build a list of points
        wktConverted = wktConverted.replace("POLYGON((", "").replace("))", "");
        coords = wktConverted.split(",");
        for (int i = 0; i < coords.length; i++) {
            String[] p = coords[i].split(" ");
            float x = Float.parseFloat(p[0]);
            float y = Float.parseFloat(p[1]);
            Coordinates point = new Coordinates(x, y, 0);
            result.add(point);
        }
        return result;
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
