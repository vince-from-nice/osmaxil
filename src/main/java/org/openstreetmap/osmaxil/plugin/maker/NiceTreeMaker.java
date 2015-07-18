package org.openstreetmap.osmaxil.plugin.maker;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.TreeElement;
import org.openstreetmap.osmaxil.model.TreeImport;
import org.openstreetmap.osmaxil.model.misc.ElementTagNames;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlNode;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlTag;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.TreeMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractParser;
import org.openstreetmap.osmaxil.plugin.common.parser.NiceTreeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("NiceTreeMaker")
public class NiceTreeMaker extends AbstractMakerPlugin<TreeElement, TreeImport> {
    
    @Autowired
    private NiceTreeParser parser;

    @Autowired
    private TreeMatcher matcher;

    @Value("${plugins.niceTreeMaker.changesetSourceLabel}")
    private String changesetSourceLabel;

    @Value("${plugins.niceTreeMaker.changesetComment}")
    private String changesetComment;

    private List<OsmXmlRoot> newTreesToCreate = new ArrayList<>();

    private List<Long> oldTreeIdsToDelete = new ArrayList<>();

    /**
     * Size of the box around imported trees where existing trees (which are the closest from imported trees)
     * must be deleted.
     */
    private static final double DELETING_BOX_WIDTH = 2.0;

    // =========================================================================
    // Overrided methods
    // =========================================================================
    
    @Override
    protected boolean isImportMakable(TreeImport imp) {
        // TODO Check if there's no existing tree too closed
        return true;
    }

    @Override
    protected void processImport(TreeImport tree) {
        this.newTreesToCreate.add(createNewTree(tree));
        this.oldTreeIdsToDelete.add(findOldTreeToDelete(tree));
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
        for (Long osmId : this.oldTreeIdsToDelete) {
            OsmXmlNode node = new OsmXmlNode();
            node.id = osmId;
            node.action = "delete";
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
    
    // =========================================================================
    // Private methods
    // =========================================================================

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
        root.nodes.add(node);
        return root;
    }

    private Long findOldTreeToDelete(TreeImport tree) {
        // Find in the deleting area the existing tree which is the closest to the imported tree
        String query = "SELECT osm_id, ST_Distance(way, ST_GeomFromText('";
        query += "POINT(" + tree.getLongitude() + " " + tree.getLatitude() + ")', " + this.osmPostgis.getSrid() + ")) as distance ";
        query += "from planet_osm_point n where n.natural = 'tree' and way && ";
        query += "ST_MakeEnvelope(" + (tree.getLongitude() - DELETING_BOX_WIDTH / 2);
        query += ", " + (tree.getLatitude() - DELETING_BOX_WIDTH / 2);
        query += ", " + (tree.getLongitude() + DELETING_BOX_WIDTH / 2);
        query += ", " + (tree.getLatitude() + DELETING_BOX_WIDTH / 2);
        query += ", " + this.osmPostgis.getSrid() + ") ORDER BY distance;";
        Long[] oldTreeIds = this.osmPostgis.findElementIdsByQuery(query);
        if (oldTreeIds.length == 0) {
            return null;    
        } else {
            return oldTreeIds[0];
        }
    }

}
