package org.openstreetmap.osmaxil.plugin.building;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.api.OsmApiRelation;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;
import org.openstreetmap.osmaxil.model.api.OsmApiTag;
import org.openstreetmap.osmaxil.model.api.OsmApiWay;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.plugin.AbstracRemakerPlugin;
import org.openstreetmap.osmaxil.plugin.loader.ParisDataCsvBuildingLoader;
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
        OsmApiRoot root = new OsmApiRoot();
        element.setRemakingData(root);
        
        // Create relation
        OsmApiRelation relation = new OsmApiRelation();
        // Reuse all tags from original element
        relation.tags = element.getTags();
        // Set a negative ID based on the original element ID
        relation.id = -element.getOsmId();
        
        // Add it into the root relation list
        root.relations = new ArrayList<OsmApiRelation>();
        root.relations.add(relation);

        // For each matching import, create a new building part
        int i = 1;
        for (AbstractImport imp : element.getMatchingImports()) {
            BuildingImport bi = (BuildingImport) imp;
            OsmApiWay part = new OsmApiWay();
            // Set a negative ID based on the original element ID + index
            part.id = -element.getOsmId() - i;
            part.tags = new ArrayList<OsmApiTag>();
            OsmApiTag tag = new OsmApiTag();
            tag.k = "building:part";
            tag.v = "yes";
            part.tags.add(tag);
            
            // TODO For each point of the import create a node
            String wkt = computeBuildingPartGeometry(bi);
            LOGGER.debug("Computed geometry for building part #" + i + ": " + wkt);
            
            // TODO member into the relation

            // Add part into the root way list
            root.ways = new ArrayList<OsmApiWay>();
            root.ways.add(part);
            i++;
        }
    }
    
    private String computeBuildingPartGeometry(BuildingImport imp) {
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
        // TODO Convert geometry coordinates to OSM SRID
        return wkt.toString();
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
