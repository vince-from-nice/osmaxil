package org.openstreetmap.osmaxil.plugin.common.matcher;

import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.plugin.updater.AbstractUpdaterPlugin;
import org.springframework.stereotype.Component;

@Component
public class BuildingMatcherWithoutSurface extends BuildingMatcher {

    @Override
    public float computeMatchingImportScore(BuildingImport imp) {
        return AbstractUpdaterPlugin.MAX_MATCHING_SCORE;
    }
    
}
