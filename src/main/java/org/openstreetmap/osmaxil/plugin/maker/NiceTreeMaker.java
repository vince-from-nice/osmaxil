package org.openstreetmap.osmaxil.plugin.maker;

import org.openstreetmap.osmaxil.model.tree.TreeElement;
import org.openstreetmap.osmaxil.model.tree.TreeImport;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractParser;

public class NiceTreeMaker extends AbstractMakerPlugin<TreeElement, TreeImport> {

    @Override
    public void processElement(TreeElement element) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void buildDataForCreation() {
        // TODO Auto-generated method stub
    }

    @Override
    public String getChangesetComment() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getChangesetSourceLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void displayProcessingStatistics() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void displaySynchronizingStatistics() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AbstractParser<TreeImport> getParser() {
        // TODO Auto-generated method stub
        return null;
    }

}
