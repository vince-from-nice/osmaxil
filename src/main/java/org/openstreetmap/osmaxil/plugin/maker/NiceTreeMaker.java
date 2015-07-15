package org.openstreetmap.osmaxil.plugin.maker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmaxil.model.tree.TreeElement;
import org.openstreetmap.osmaxil.model.tree.TreeImport;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlNode;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
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
    protected void processImport(TreeImport tree) {
        OsmXmlRoot root = new OsmXmlRoot();
        OsmXmlNode node = new OsmXmlNode();
        node.id = this.idGenerator.getId();
        node.lat = tree.getLatitude().toString();
        node.lon = tree.getLongitude().toString();
       //TODO add tags and other attributes
        this.trees.add(root);
    }

    @Override
    protected void buildDataForCreation() {
        // TODO Auto-generated method stub
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
