package org.openstreetmap.osmaxil.plugin.maker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.TreeElement;
import org.openstreetmap.osmaxil.model.TreeImport;
import org.openstreetmap.osmaxil.model.misc.ElementTagNames;
import org.openstreetmap.osmaxil.model.misc.ElementType;
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

    private Map<Long, TreeElement> oldTreeToDeleteById = new HashMap<Long, TreeElement>();

    /**
     * Size of the buffer around imported trees where existing trees (at least the closest one from imported trees) must
     * be deleted.
     */
    private static final double DELETING_BOX_RADIUS = 2.0;

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
    protected void processImport(TreeImport tree) {
        this.newTreesToCreate.add(createNewTree(tree));
        this.updateOldTreesToDelete(tree);
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
    protected void buildDataForDeletion() {
        OsmXmlRoot root = new OsmXmlRoot();
        for (TreeElement tree : this.oldTreeToDeleteById.values()) {
            OsmXmlNode node = new OsmXmlNode();
            node.id = tree.getOsmId();
            node.action = "delete";
            node.changeset = 0;
            node.uid = 0;
            node.version = tree.getApiData().nodes.get(0).version;
            root.nodes.add(node);
        }
        this.dataForDeletion = root;
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
        LOGGER_FOR_STATS.info("Total of deleted trees: " + oldTreeToDeleteById.size());
        LOGGER_FOR_STATS.info("Total of created trees: " + newTreesToCreate.size());
    }

    // =========================================================================
    // Private methods
    // =========================================================================

    @PostConstruct
    private void init() {
        this.matcher.setMatchingAreaRadius(DELETING_BOX_RADIUS);
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
        tag.k = ElementTagNames.NATURAL;
        tag.v = "tree";
        node.tags.add(tag);
        // Add the tag genus=*
        tag = new OsmXmlTag();
        tag.k = ElementTagNames.GENUS;
        tag.v = tree.getType();
        node.tags.add(tag);
        // Add the tag specifies=*
        tag = new OsmXmlTag();
        tag.k = ElementTagNames.SPECIFIES;
        tag.v = tree.getSubType();
        node.tags.add(tag);
        // Add the tag ref=*
        tag = new OsmXmlTag();
        tag.k = ElementTagNames.REF + REF_CODE_SUFFIX;
        tag.v = tree.getReference();
        node.tags.add(tag);
        root.nodes.add(node);
        return root;
    }

    /**
     * Find in the deleting area the existing tree which is the closest to the imported tree and place them into the
     * deleting list.
     */
    private List<TreeElement> updateOldTreesToDelete(TreeImport importedTree) {
        List<TreeElement> results = new ArrayList<>();
        List<MatchingElementId> matchingElementIds = this.matcher.findMatchingElements(importedTree,
                this.parser.getSrid());
        // Depending on the matcher settings, list of ids can have one or many elements
        for (MatchingElementId matchingElementId : matchingElementIds) {
            TreeElement tree = this.oldTreeToDeleteById.get(matchingElementId.getOsmId());
            if (tree == null) {
                tree = new TreeElement(matchingElementId.getOsmId());
                tree.setApiData(this.osmStandardApi.readElement(tree.getOsmId(), ElementType.Node));
                this.oldTreeToDeleteById.put(tree.getOsmId(), tree);
            }
        }
        return results;
    }
}
