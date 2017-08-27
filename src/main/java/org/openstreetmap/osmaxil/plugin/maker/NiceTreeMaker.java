package org.openstreetmap.osmaxil.plugin.maker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.model.ElementType;
import org.openstreetmap.osmaxil.model.TreeElement;
import org.openstreetmap.osmaxil.model.TreeImport;
import org.openstreetmap.osmaxil.model.misc.MatchingElementId;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlNode;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlTag;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractImportMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.TreeImportMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractImportParser;
import org.openstreetmap.osmaxil.plugin.common.parser.NiceTreeImportParser2015;
import org.openstreetmap.osmaxil.plugin.common.selector.AbstractMatchingScoreSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("NiceTreeMaker") @Lazy
public class NiceTreeMaker extends AbstractMakerPlugin<TreeElement, TreeImport> {

    @Autowired
    private NiceTreeImportParser2015 parser;

    @Autowired
    private TreeImportMatcher matcher;

    @Value("${plugins.niceTreeMaker.changesetSourceLabel}")
    private String changesetSourceLabel;

    @Value("${plugins.niceTreeMaker.changesetComment}")
    private String changesetComment;

    private List<OsmXmlRoot> newTreesToCreate = new ArrayList<>();

    private Map<Long, TreeElement> existingTreesById = new HashMap<Long, TreeElement>();
    
    private Map<Long, Float> bestScoreByExistingTreeId = new HashMap<Long, Float>();
    
    private Map<Long, TreeImport> bestImportedTreeByExistingTreeId = new HashMap<Long, TreeImport>();
    
    private Map<Long, List<MatchingElementId>> matchingTreeIdsByImportTreeId = new HashMap<>();
    
    private List<TreeImport> nonMakableImportedTrees = new ArrayList<TreeImport>();
    
    private int counterForMultiMatchingTrees;
    
    private boolean useReferenceCode = false; // apparently setting a tag for the internal reference is not a good thing (!)

    /**
     * Size of the buffer around imported trees where existing trees (at least the closest one from imported trees) must
     * be updated or deleted.
     */
    private static final double MATCHING_BOX_RADIUS = 3.0;

    private static final String REF_CODE_SUFFIX = ":FR:Nice:trees";

    // =========================================================================
    // Overrided methods
    // =========================================================================

    @Override
    protected boolean isImportMakable(TreeImport imp) {
        // Check if the imported tree is inside an existing building
        if (this.isTreeInsideExistingBuilding(imp)) {
            LOGGER.warn("Tree #" + imp.getId() + " is inside an existing building => it's not makable");
            this.nonMakableImportedTrees.add(imp);
            return false;
        }
        return true;
    }
    
    @Override
    protected void processImport(TreeImport importedTree) {
        List<MatchingElementId> matchingElementIds = this.getMatchingTreesByImportedTree(importedTree);
        // If there's no matching tree, create a new tree from the import
        if (matchingElementIds.isEmpty()) {
            LOGGER.info("Tree has no matching tree, create a new tree from the import...");
            this.newTreesToCreate.add(this.createNewTreeFromImport(importedTree));
        } 
        // Else watch was already done before...
        else {
            MatchingElementId bestMatchingElementId = matchingElementIds.get(0);
            long bestMatchingOsmId = bestMatchingElementId.getOsmId();
            LOGGER.info("Tree matches existing tree #" + bestMatchingOsmId);
            TreeElement existingTree = this.existingTreesById.get(bestMatchingOsmId);
            // if the best existing tree was not yet used create it and keep it
            if (existingTree == null) {
                existingTree = this.createNewTreeFromExistingTree(bestMatchingOsmId, importedTree);
                this.existingTreesById.put(existingTree.getOsmId(), existingTree);
                this.bestScoreByExistingTreeId.put(bestMatchingOsmId, bestMatchingElementId.getScore());
                this.bestImportedTreeByExistingTreeId.put(existingTree.getOsmId(), importedTree);
            }
            // Else the matching tree is already matching another imported tree, need to do a special process
            else {
                this.counterForMultiMatchingTrees++;
                LOGGER.info("Best existing tree#" + bestMatchingOsmId + " is matching with more than one imported tree");
                processMultiMatchingTree(importedTree, 0);
            }
        }
    }
    
    @Override
    protected void buildDataForCreation() {
        OsmXmlRoot root = new OsmXmlRoot();
        for (OsmXmlRoot tree : this.newTreesToCreate) {
            root.nodes.add(tree.nodes.get(0));
        }
        this.dataForCreation = root;
    }

    @Override
    protected void buildDataForModification() {
        OsmXmlRoot root = new OsmXmlRoot();
        for (TreeElement tree : this.existingTreesById.values()) {
            root.nodes.add(tree.getApiData().nodes.get(0));
        }
        this.dataForModification = root;
    }

    @Override
    protected void buildDataForDeletion() {
    }

    @Override
    protected void buildDataForNonMakableElements() {
        OsmXmlRoot root = new OsmXmlRoot();
        for (TreeImport importedTree : this.nonMakableImportedTrees) {
            OsmXmlRoot xml = this.createNewTreeFromImport(importedTree);
            root.nodes.add(xml.nodes.get(0));
        }
        this.dataForNonMakableElements = root;
    }
    
    @Override
    public String getChangesetComment() {
        return this.changesetComment;
    }
    
    @Override
    public String getChangesetSourceLabel() {
        return this.changesetSourceLabel;
    }

    @Override
    public AbstractImportParser<TreeImport> getParser() {
        return this.parser;
    }

    @Override
    protected AbstractImportMatcher<TreeImport> getMatcher() {
        return this.matcher;
    }

    @Override
    public void displayProcessingStatistics() {
        super.displayProcessingStatistics();
        LOGGER_FOR_STATS.info("Matching area radius: " + MATCHING_BOX_RADIUS);
        LOGGER_FOR_STATS.info("Total of created trees: " + this.newTreesToCreate.size());
        LOGGER_FOR_STATS.info("Total of updated trees: " + this.existingTreesById.size());
        LOGGER_FOR_STATS.info("Total of created or updated trees: " + (newTreesToCreate.size() + existingTreesById.size()));
        LOGGER_FOR_STATS.info("Total of multi matching trees: " + this.counterForMultiMatchingTrees);
    }

    // =========================================================================
    // Private methods
    // =========================================================================

    @PostConstruct
    private void init() {
        this.matcher.setMatchingAreaRadius(MATCHING_BOX_RADIUS);
        this.matcher.setMatchClosestOnly(false);
    }
    
    private boolean isTreeInsideExistingBuilding(TreeImport imp) {
        String geom = "ST_GeomFromText('POINT(" + imp.getLongitude() + " " + imp.getLatitude() + ")', " + this.parser.getSrid() + ")";
        // Transform geometry if it's needed
        if (this.parser.getSrid() != this.osmPostgis.getSrid()) {
            geom = "ST_Transform(" + geom + ", " + this.osmPostgis.getSrid() + ")";
        }
        String query = "select osm_id, 1 from planet_osm_polygon where building <> '' and  ST_Contains(way, " + geom + ");";
        LOGGER.debug("Looking in PostGIS for buildings containing coords: " + query);
        Long[] ids = this.osmPostgis.findElementIdsByQuery(query);
        if (ids.length > 0) {
            return true;
        } else {
            return false;
        }
    }

    private OsmXmlRoot createNewTreeFromImport(TreeImport tree) {
        OsmXmlRoot root = new OsmXmlRoot();
        OsmXmlNode node = new OsmXmlNode();
        node.id = -this.idGenerator.getId();
        node.version = 0;
        node.lat = tree.getLatitude().toString();
        node.lon = tree.getLongitude().toString();
        // Add the tag natural=*
        OsmXmlTag tag = new OsmXmlTag();
        tag.k = ElementTag.NATURAL;
        tag.v = "tree";
        node.tags.add(tag);
        // Add the tag ref=*
        if (this.useReferenceCode) {
            tag = new OsmXmlTag();
            tag.k = ElementTag.REF + REF_CODE_SUFFIX;
            tag.v = tree.getReference();
            node.tags.add(tag);
        }
//        // Add the tag genus=*
//        tag = new OsmXmlTag();
//        tag.k = ElementTag.GENUS;
//        tag.v = tree.getType();
//        node.tags.add(tag);
//        // Add the tag specifies=*
//        tag = new OsmXmlTag();
//        tag.k = ElementTag.SPECIFIES;
//        tag.v = tree.getSubType();
//        node.tags.add(tag);
        root.nodes.add(node);
        return root;
    }

    private TreeElement createNewTreeFromExistingTree(long osmId, TreeImport importedTree) {
        TreeElement tree = new TreeElement(osmId);
        // Fetch data from the API
        tree.setApiData(this.osmStandardApi.readElement(tree.getOsmId(), tree.getType()));
        // Flag it as an element to modify
        tree.getApiData().nodes.get(0).action = "modify";
        // Move existing tree to the same position than the imported tree
        tree.setLatitude(importedTree.getLatitude());
        tree.setLongitude(importedTree.getLongitude());
        // And add the reference tag
        if (this.useReferenceCode) {
            tree.setTagValue(ElementTag.REF + REF_CODE_SUFFIX, importedTree.getReference());
        }
        return tree;
    }
    
    
    private void processMultiMatchingTree(TreeImport importedTree, int matchingElementIndex) {
        List<MatchingElementId> matchingElementIds = this.getMatchingTreesByImportedTree(importedTree);
        MatchingElementId matchingElementId = matchingElementIds.get(matchingElementIndex);
        LOGGER.info("Process multi matching tree for imported tree #" + importedTree.getId() + " with index="
                + (1 + matchingElementIndex) + "/" + matchingElementIds.size());
        long matchingOsmId = matchingElementId.getOsmId();
        Float previousBestScore = this.bestScoreByExistingTreeId.get(matchingElementId.getOsmId());
        // If we have a new winner (ie. it's closer to the best existing tree than the previous closest imported tree)
        if (previousBestScore == null || matchingElementId.getScore() > previousBestScore) {
            // The new best existing tree must be updated instead of created
            String txt = "Imported tree is closer to the existing tree #" + matchingOsmId + " than the previous closest imported tree ";
            if (previousBestScore != null) { 
                txt += "(" + 1 / matchingElementId.getScore() + " < " + 1 / this.bestScoreByExistingTreeId.get(matchingElementId.getOsmId()) + ")";
            } else {
                txt += "(no previous best score yet)";
            }
            txt += " => can update it instead of create a new tree";
            LOGGER.info(txt); 
            TreeImport previousBestImportedTree = this.bestImportedTreeByExistingTreeId.get(matchingOsmId);
            TreeElement newExistingTree = this.createNewTreeFromExistingTree(matchingOsmId, importedTree);
            this.existingTreesById.put(newExistingTree.getOsmId(), newExistingTree);
            this.bestScoreByExistingTreeId.put(newExistingTree.getOsmId(), matchingElementId.getScore());
            this.bestImportedTreeByExistingTreeId.put(newExistingTree.getOsmId(), importedTree);
            // If there was a previous best existing tree, its related imported tree must be reprocessed
            if (previousBestImportedTree != null) {
                LOGGER.info("Need to reprocess the previous best closest imported tree #" + previousBestImportedTree.getId());
                this.processMultiMatchingTree(previousBestImportedTree, 0);
            }
        }
        else {
            String txt = "Imported tree is NOT closer to the existing tree #" + matchingOsmId + " than the previous closest imported tree ";
            if (previousBestScore != null) { 
                txt += "(" + 1 / matchingElementId.getScore() + " > " + 1 / this.bestScoreByExistingTreeId.get(matchingElementId.getOsmId()) + ")";
            }
            LOGGER.info(txt); 
            // If there is another matching element for that import, retry with it
            if (matchingElementIndex < matchingElementIds.size() - 1) {
                this.processMultiMatchingTree(importedTree, ++matchingElementIndex);
            }
            // Else the imported tree cannot update an existing tree, it must created
            else {
                LOGGER.info("Imported tree doesn't have another matching tree => cannot update any existing tree, the tree must created");
                this.newTreesToCreate.add(createNewTreeFromImport(importedTree));
            }
        }
    }
    
    private List<MatchingElementId> getMatchingTreesByImportedTree(TreeImport importedTree) {
        List<MatchingElementId> matchingElementIds = this.matchingTreeIdsByImportTreeId.get(importedTree.getId());
        // If the matching tree has not yet been calculated for that imported tree do it
        if (matchingElementIds == null) {
            matchingElementIds = this.matcher.findMatchingElements(importedTree, this.parser.getSrid());
            this.matchingTreeIdsByImportTreeId.put(importedTree.getId(), matchingElementIds);
        }
        return matchingElementIds;
    }
}
