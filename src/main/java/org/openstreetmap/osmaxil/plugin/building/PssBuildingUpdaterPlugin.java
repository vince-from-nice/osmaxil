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
        if (ElementTagNames.HEIGHT.equals(tagName)) {
            element.setHeight(Float.parseFloat(tagValue));
            LOGGER.info("===> Updating height to " + tagValue);
            updated = true;
        }
        if (ElementTagNames.URL.equals(tagName)) {
            element.setTagValue(ElementTagNames.URL, tagValue);
            LOGGER.info("===> Updating URL to " + tagValue);
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
        // There's no way to compute a matching score for now with PSS (building area is not available)
        return 1.0f;
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

