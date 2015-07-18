package org.openstreetmap.osmaxil.plugin.common.matcher;

import java.util.List;

import org.openstreetmap.osmaxil.model.TreeImport;
import org.openstreetmap.osmaxil.model.misc.MatchingElementId;
import org.springframework.stereotype.Component;

@Component
public class TreeMatcher extends AbstractMatcher<TreeImport> {

    @Override
    public List<MatchingElementId> findMatchingImport(TreeImport tree, int srid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float computeMatchingImportScore(TreeImport tree) {
        // TODO Auto-generated method stub
        return 0;
    }

}
