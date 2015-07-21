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
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.TreeMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractParser;
import org.openstreetmap.osmaxil.plugin.common.parser.NiceTreeParser2015;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("NiceTreeMaker")
public class NiceTreeMaker extends AbstractMakerPlugin<TreeElement, TreeImport> {

    @Autowired
    private NiceTreeParser2015 parser;

    @Autowired
    private TreeMatcher matcher;

    @Value("${plugins.niceTreeMaker.changesetSourceLabel}")
    private String changesetSourceLabel;

    @Value("${plugins.niceTreeMaker.changesetComment}")
    private String changesetComment;

    private List<OsmXmlRoot> newTreesToCreate = new ArrayList<>();

    private Map<Long, TreeElement> matchingTreesById = new HashMap<Long, TreeElement>();
    
    private int counterForMultiMatchingTrees;

    /**
     * Size of the buffer around imported trees where existing trees (at least the closest one from imported trees) must
     * be updated or deleted.
     */
    private static final double MATCHING_BOX_RADIUS = 2.0;

    private static final String REF_CODE_SUFFIX = ":FR:Nice:trees";

    // =========================================================================
    // Overrided methods
    // =========================================================================

    @Override
    protected boolean isImportMakable(TreeImport imp) {
        // Nothing special to check with trees, true is always returned..
        return true;
    }

    @Override
    protected void processImport(TreeImport importedTree) {
        List<MatchingElementId> matchingElementIds = this.matcher.findMatchingElements(importedTree,
                this.parser.getSrid());
        if (matchingElementIds.isEmpty()) {
            LOGGER.info("Tree has no match, need to create a new one...");
            this.newTreesToCreate.add(createNewTree(importedTree));
        } else {
            LOGGER.info("Tree has a match, need to modify existing tree...");
            TreeElement tree = this.matchingTreesById.get(matchingElementIds.get(0).getOsmId());
            // if tree is not yet present in the map create a new one
            if (tree == null) {
                tree = new TreeElement(matchingElementIds.get(0).getOsmId());
                this.matchingTreesById.put(tree.getOsmId(), tree);
                tree.setApiData(this.osmStandardApi.readElement(tree.getOsmId(), ElementType.Node));
                // Move existing tree to the same position than the imported tree
                tree.setLatitude(importedTree.getLatitude());
                tree.setLongitude(importedTree.getLongitude());
                // And add the reference tag
                OsmXmlTag tag = new OsmXmlTag();
                tree.setTagValue(ElementTag.REF + REF_CODE_SUFFIX, importedTree.getReference());
                tree.getApiData().nodes.get(0).tags.add(tag);
            }
            // Else the matching tree is already matching another imported tree
            else {
                this.counterForMultiMatchingTrees++;
                LOGGER.warn("Existing tree#" + tree.getOsmId() + " is matching with more than one imported tree");
                // In that case we create a new tree based on the new imported tree
                // TOdO A better solution would be to keep the closest matching tree and for the other ones create a new tree
                this.newTreesToCreate.add(createNewTree(importedTree));
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
        for (TreeElement tree : this.matchingTreesById.values()) {
            OsmXmlNode node = new OsmXmlNode();
            node.id = tree.getOsmId();
            node.action = "modify";
            node.changeset = 0;
            node.uid = 0;
            node.version = tree.getApiData().nodes.get(0).version;
            root.nodes.add(node);
        }
        this.dataForModification = root;
    }

    @Override
    protected void buildDataForDeletion() {
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
    public AbstractParser<TreeImport> getParser() {
        return this.parser;
    }

    @Override
    protected AbstractMatcher<TreeImport> getMatcher() {
        return this.matcher;
    }

    @Override
    public void displayProcessingStatistics() {
        super.displayProcessingStatistics();
        LOGGER_FOR_STATS.info("Total of created trees: " + newTreesToCreate.size());
        LOGGER_FOR_STATS.info("Total of updated trees: " + matchingTreesById.size());
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

    private OsmXmlRoot createNewTree(TreeImport tree) {
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
        tag = new OsmXmlTag();
        tag.k = ElementTag.REF + REF_CODE_SUFFIX;
        tag.v = tree.getReference();
        node.tags.add(tag);
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

    /**
     * Find in the deleting area the existing tree which is the closest to the imported tree, do some modification on it
     * and then store it into the internal map.
     */
    private List<TreeElement> updateOldTreesToModify(TreeImport importedTree) {
        List<TreeElement> results = new ArrayList<>();
        return results;
    }
}
