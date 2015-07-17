package org.openstreetmap.osmaxil.plugin.maker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.misc.ElementTagNames;
import org.openstreetmap.osmaxil.model.tree.TreeElement;
import org.openstreetmap.osmaxil.model.tree.TreeImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlNode;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlTag;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractParser;
import org.openstreetmap.osmaxil.plugin.common.parser.NiceTreeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("NiceTreeMaker")
public class NiceTreeMaker extends AbstractMakerPlugin<TreeElement, TreeImport> {

    @Autowired
    private NiceTreeParser parser;
    
    @Value("${plugins.niceTreeMaker.changesetSourceLabel}")
    private String changesetSourceLabel;

    @Value("${plugins.niceTreeMaker.changesetComment}")
    private String changesetComment;
    
    private List<OsmXmlRoot> trees = new ArrayList<>();
    
    @Override
    protected boolean isImportMakable(TreeImport imp) {
        // TODO Check if there's no existing tree too closed
        return true;
    }
    
    @Override
    protected void processImport(TreeImport tree) {
        OsmXmlRoot root = new OsmXmlRoot();
        OsmXmlNode node = new OsmXmlNode();
        node.id = this.idGenerator.getId();
        node.lat = tree.getLatitude().toString();
        node.lon = tree.getLongitude().toString();
        // Add the tag natural=*
        OsmXmlTag tag = new OsmXmlTag();
        tag.k =ElementTagNames.NATURAL;
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
        this.trees.add(root);
    }

    @Override
    protected void buildData() {
        OsmXmlRoot root = new OsmXmlRoot();
        root.version = 0.6f;
        root.generator = Application.NAME;
        for (OsmXmlRoot tree : this.trees) {
            root.nodes.add(tree.nodes.get(0));
        }
        this.data = root;
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

}
