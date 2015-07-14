package org.openstreetmap.osmaxil.plugin.maker;

import org.openstreetmap.osmaxil.model.tree.TreeElement;
import org.openstreetmap.osmaxil.model.tree.TreeImport;
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
    
    @Override
    protected void processImport(TreeImport tree) {
        // TODO Auto-generated method stub
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
