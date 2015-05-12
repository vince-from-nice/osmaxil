package org.openstreetmap.osmaxil.plugin.building;

import java.util.List;

import org.openstreetmap.osmaxil.model.ElementTagNames;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.plugin.AbstractUpdaterPlugin;
import org.openstreetmap.osmaxil.plugin.parser.PssBuildingParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component ("PssBuildingUpdaterPlugin")
public class PssBuildingUpdaterPlugin extends AbstractUpdaterPlugin<BuildingElement, BuildingImport> {

    @Autowired
    private PssBuildingParser loader;
    
    @Autowired
    private BuildingHelper helper;
    
    @Value("${plugins.pssBuildingUpdater.updatableTagNames}")
    private String updatableTagNames;
    
    @Value("${plugins.pssBuildingUpdater.minMatchingScore}")
    private float minMatchingScore;

    @Value("${plugins.pssBuildingUpdater.changesetSourceLabel}")
    private String changesetSourceLabel;
    
    @Value("${plugins.pssBuildingUpdater.changesetComment}")
    private String changesetComment;

    @Override
    public boolean isElementTagUpdatable(BuildingElement element, String tagName) {
        // Building tags are updatable only if it doesn't have an original value
        return element.getOriginalValuesByTagNames().get(tagName) == null;
    }

    @Override
    public boolean updateElementTag(BuildingElement element, String tagName) {
        String tagValue = element.getBestTagValueByTagName(tagName);
        if (tagValue == null) {
            LOGGER.warn("Cannot update tag because best tag value is null for " + tagName);
            return false;
        }
        boolean updated = false;
        if (ElementTagNames.BUILDING_LEVELS.equals(tagName)) {
            // Adding +1 to levels because OSM use the US way to count building levels
            element.setLevels(Integer.parseInt(tagValue) + 1);
            LOGGER.info("===> Updating levels to " + (tagValue + 1));
            updated = true;
        }
        if (ElementTagNames.URL.equals(tagName)) {
            element.setTagValue(ElementTagNames.URL, tagValue);
            element.setLevels(Integer.parseInt(tagValue) + 1);
            LOGGER.info("===> Updating levels to " + (tagValue + 1));
            updated = true;
        }
        return updated;
    }

    @Override
    public List<MatchingElementId> findMatchingElements(BuildingImport imp) {
       return this.helper.findMatchingBuildings(imp, this.getParser().getSrid());
    }

    @Override
    public float computeMatchingScore(BuildingImport imp) {
        return this.helper.computeBuildingMatchingScore(imp);
    }

    @Override
    public BuildingElement instanciateElement(long osmId) {
        return new BuildingElement(osmId);
    }
    
    @Override
    public String[] getUpdatableTagNames() {
        return updatableTagNames.split(",");
    }

    @Override
    public PssBuildingParser getParser() {
        return loader;
    }

    @Override
    public String getChangesetSourceLabel() {
        return changesetSourceLabel;
    }

    @Override
    public String getChangesetComment() {
        return changesetComment;
    }

    @Override
    public float getMinMatchingScore() {
        return this.minMatchingScore;
    }
    
}

