package org.openstreetmap.osmaxil.plugin.updater;

import org.openstreetmap.osmaxil.model.ElementTagNames;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.BuildingMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.PssBuildingParser;
import org.openstreetmap.osmaxil.plugin.common.scorer.AbstractMatchingScorer;
import org.openstreetmap.osmaxil.plugin.common.scorer.ExclusiveMatchingScorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component ("PssBuildingUpdaterPlugin")
public class PssBuildingUpdaterPlugin extends AbstractUpdaterPlugin<BuildingElement, BuildingImport> {

    @Autowired
    private PssBuildingParser parser;

    @Autowired
    private BuildingMatcher matcher;
    @Autowired
    private ExclusiveMatchingScorer<BuildingElement> scorer;
    
    @Value("${plugins.pssBuildingUpdater.minMatchingScore}")
    private float minMatchingScore;

    @Value("${plugins.pssBuildingUpdater.changesetSourceLabel}")
    private String changesetSourceLabel;
    
    @Value("${plugins.pssBuildingUpdater.changesetComment}")
    private String changesetComment;
    
    private static final String UPDATABLE_TAG_NAMES[] = new String[] {ElementTagNames.HEIGHT, ElementTagNames.URL};

    @Override
    public float computeImportMatchingScore(BuildingImport imp) {
        // There's no way to compute a matching score for now with PSS (building area is not available)
        return AbstractPlugin.MAX_MATCHING_SCORE;
    }
    
    @Override
    public boolean isElementTagUpdatable(BuildingElement element, String tagName) {
        // Building tags are updatable only if it doesn't have an original value
        return element.getOriginalValuesByTagNames().get(tagName) == null;
    }

    @Override
    public boolean updateElementTag(BuildingElement element, String tagName) {
        String tagValue = element.getMatchingImports().get(0).getTagValue(tagName);
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
    public  void displayStatistics() {
        // TODO return stats from the matcher
        LOGGER.info("No available stats for this plugin");
    }
    
    @Override
    public String[] getUpdatableTagNames() {
        return UPDATABLE_TAG_NAMES;
    }

    @Override
    public BuildingElement instanciateElement(long osmId) {
        return new BuildingElement(osmId);
    }

    @Override
    public PssBuildingParser getParser() {
        return parser;
    }
    
    @Override
    public AbstractMatcher<BuildingImport> getMatcher() {
        return this.matcher;
    }

    @Override
    public AbstractMatchingScorer<BuildingElement> getScorer() {
      return this.scorer;
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
    public float getMinimalMatchingScore() {
        return this.minMatchingScore;
    }
    
}

