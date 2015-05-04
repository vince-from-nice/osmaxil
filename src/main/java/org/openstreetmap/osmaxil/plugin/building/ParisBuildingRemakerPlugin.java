package org.openstreetmap.osmaxil.plugin.building;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.Point;
import org.openstreetmap.osmaxil.model.api.OsmApiMember;
import org.openstreetmap.osmaxil.model.api.OsmApiNd;
import org.openstreetmap.osmaxil.model.api.OsmApiNode;
import org.openstreetmap.osmaxil.model.api.OsmApiRelation;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;
import org.openstreetmap.osmaxil.model.api.OsmApiTag;
import org.openstreetmap.osmaxil.model.api.OsmApiWay;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.plugin.AbstracRemakerPlugin;
import org.openstreetmap.osmaxil.plugin.loader.ParisDataCsvBuildingLoader;
import org.openstreetmap.osmaxil.util.IdIncrementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class ParisBuildingRemakerPlugin extends AbstracRemakerPlugin<BuildingElement, BuildingImport> {

    @Autowired
    private ParisDataCsvBuildingLoader loader;

    @Autowired
    private BuildingHelper helper;

    @Value("${plugins.parisBuildingMaker.minMatchingScore}")
    private float minMatchingScore;

    @Value("${plugins.parisBuildingMaker.changesetSourceLabel}")
    private String changesetSourceLabel;

    @Value("${plugins.parisBuildingMaker.changesetComment}")
    private String changesetComment;

    @Override
    public List<MatchingElementId> findMatchingElements(BuildingImport imp) {
        return this.helper.findMatchingBuildings(imp);
    }

    @Override
    public float computeMatchingScore(BuildingImport imp) {
        return this.helper.computeMatchingScore(imp);
    }

    @Override
    public BuildingElement instanciateElement(long osmId) {
        return new BuildingElement(osmId);
    }

    @Override
    public void buildRemakedElements(BuildingElement element) {
        LOGGER.debug("Building XML for remaking of element #" + element.getOsmId() + ":");
        OsmApiRoot root = new OsmApiRoot();
        IdIncrementor idGen = new IdIncrementor(1);
        
        // Instanciate sublists
        root.relations = new ArrayList<OsmApiRelation>();
        root.nodes = new ArrayList<OsmApiNode>();
        
        // Create relation
        OsmApiRelation relation = new OsmApiRelation();
        // Reuse all tags from original element
        relation.tags = element.getTags();
        // Set a negative ID based on the original element ID
        relation.id = -element.getOsmId();
        // Instanciate sublists
        relation.members = new ArrayList<OsmApiMember>();
        // Add it into the root relation list
        root.relations.add(relation);
        
        // For each matching import:
        for (AbstractImport imp : element.getMatchingImports()) {
            BuildingImport bi = (BuildingImport) imp;
            
            // Create a new building part
            OsmApiWay part = new OsmApiWay();
            // Set a negative ID based on the original element ID + index
            part.id = - idGen.getId();
            LOGGER.debug("\tBuilding part id=" + part.id);
            // Instanciate sublists
            part.nds = new ArrayList<OsmApiNd>();
            part.tags = new ArrayList<OsmApiTag>();
            // Add the building:part tag
            OsmApiTag tag = new OsmApiTag();
            tag.k = "building:part";
            tag.v = "yes";
            part.tags.add(tag);
            
            // Add member into the relation
            OsmApiMember member = new OsmApiMember();
            member.ref = part.id;
            member.role = "part";
            member.type = "way";
            relation.members.add(member);
            
            // Add part into the root way list
            root.ways = new ArrayList<OsmApiWay>();
            root.ways.add(part);
            
            // For each point of the import:
            List<Point> points = computeBuildingPartGeometry(bi);
            for (Point point : points) {
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
        
        element.setRemakingData(root);
    }
    

    // TODO Eventually move that method into ParisData CSV loader
    private List<Point> computeBuildingPartGeometry(BuildingImport imp) {
        List<Point> result = new ArrayList<Point>();
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
        int srid = ((ParisDataCsvBuildingLoader) this.getLoader()).getSrid();
        String wktConverted = this.osmPostgis.tranformGeometry(wkt.toString(), srid);
        // Reparse transformed geometry to build a list of points
        wktConverted = wktConverted.replace("POLYGON((", "").replace("))", "");
        coords = wktConverted.split(",");
        for (int i = 0; i < coords.length; i++) {
            String[] p = coords[i].split(" ");
            float x = Float.parseFloat(p[0]);
            float y = Float.parseFloat(p[1]);
            Point point = new Point(x, y);
            result.add(point);
        }
        return result;
    }

    // @Override
    // public void buildRemakedElements(BuildingElement element) {
    // // For each matching import, create a new building part
    // List<BuildingPart> parts = new ArrayList<>();
    // for (AbstractImport imp : element.getMatchingImports()) {
    // BuildingPart bp = this.helper.createBuildingPart((BuildingImport) imp);
    // parts.add(bp);
    // }
    // // Create a global relation of type building
    // BuildingRelation br = this.helper.createBuildingRelation(element);
    // // For each building part, add a member into the relation
    // for (BuildingPart bp : parts) {
    // OsmApiMember member = new OsmApiMember();
    // member.ref = 0; // TODO
    // member.role = "part";
    // member.type = "way";
    // }
    // // Store building relation and parts
    // element.getNewElementsForRemaking().add(br);
    // element.getNewElementsForRemaking().addAll(parts);
    // }

    @Override
    public float getMinMatchingScore() {
        return minMatchingScore;
    }

    public ParisDataCsvBuildingLoader getLoader() {
        return loader;
    }

    public void setLoader(ParisDataCsvBuildingLoader loader) {
        this.loader = loader;
    }

    public String getChangesetSourceLabel() {
        return changesetSourceLabel;
    }

    public void setChangesetSourceLabel(String changesetSourceLabel) {
        this.changesetSourceLabel = changesetSourceLabel;
    }

    public String getChangesetComment() {
        return changesetComment;
    }

    public void setChangesetComment(String changesetComment) {
        this.changesetComment = changesetComment;
    }
}
