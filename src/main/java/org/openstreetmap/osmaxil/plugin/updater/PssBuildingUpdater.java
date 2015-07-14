package org.openstreetmap.osmaxil.plugin.updater;

import org.openstreetmap.osmaxil.model.ElementTagNames;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.BuildingMatcherWithoutSurface;
import org.openstreetmap.osmaxil.plugin.common.parser.PssBuildingParser;
import org.openstreetmap.osmaxil.plugin.common.scorer.AbstractMatchingScorer;
import org.openstreetmap.osmaxil.plugin.common.scorer.ExclusiveMatchingScorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component ("PssBuildingUpdater")
public class PssBuildingUpdater extends AbstractUpdaterPlugin<BuildingElement, BuildingImport> {

    @Autowired
    private PssBuildingParser parser;

    @Autowired
    private BuildingMatcherWithoutSurface matcher;
    
    @Autowired
    private ExclusiveMatchingScorer<BuildingElement> scorer;
    
    @Value("${plugins.pssBuildingUpdater.minMatchingScore}")
    private float minMatchingScore;

    @Value("${plugins.pssBuildingUpdater.changesetSourceLabel}")
    private String changesetSourceLabel;
    
    @Value("${plugins.pssBuildingUpdater.changesetComment}")
    private String changesetComment;
    
    private int counterForFakeNames = 0;
            
    private static final String UPDATABLE_TAG_NAMES[] = new String[] {ElementTagNames.HEIGHT, ElementTagNames.NAME, ElementTagNames.URL};
    
    @Override
    protected boolean isElementTagUpdatable(BuildingElement element, String tagName) {
        // Building tags are updatable only if it doesn't have an original value, except for the tag url which can have multiples values
        return (ElementTagNames.URL.equals(tagName) ? true : element.getOriginalValuesByTagNames().get(tagName) == null);
    }

    @Override
    protected boolean updateElementTag(BuildingElement element, String tagName) {
        String tagValue = element.getMatchingImports().get(0).getValueByTagName(tagName);
        if (tagValue == null) {
            LOGGER.warn("Cannot update tag because tag value is null for " + tagName);
            return false;
        }
        boolean updated = false;
        if (ElementTagNames.HEIGHT.equals(tagName)) {
            element.setHeight(Float.parseFloat(tagValue));
            LOGGER.info("===> Updating height to [" + tagValue + "]");
            updated = true;
        }
        if (ElementTagNames.URL.equals(tagName)) {
            element.setTagValue(ElementTagNames.URL, tagValue);
            LOGGER.info("===> Updating URL to [" + tagValue + "]");
            updated = true;
        }
        if (ElementTagNames.NAME.equals(tagName)) {
            // Ignore fake name based on the building address
           if (Character.isDigit(tagValue.charAt(0))) {
                LOGGER.info("Skipping name update because the value looks fake (" + tagValue + ")");
                this.counterForFakeNames++;
            } else {
                element.setTagValue(ElementTagNames.NAME, tagValue);
                LOGGER.info("===> Updating name to [" + tagValue + "]");
                updated = true;
            }
        }
        return updated;
    }
    
    // TEMP just for stats: elements are updatable if all of the updatable tags are updatable with it
//    @Override
//    public boolean isElementAlterable(BuildingElement element) {
//        for (int j = 0; j < this.getUpdatableTagNames().length; j++) {
//            if(!this.isElementTagUpdatable(element, this.getUpdatableTagNames()[j])) {
//                return false;
//            }
//        }
//        return true;
//    }
    
    @Override
    public  void displaySynchronizingStatistics(){
        super.displaySynchronizingStatistics();
        LOGGER_FOR_STATS.info("Total of fake names: " + this.counterForFakeNames);
    }
    
    @Override
    protected String[] getUpdatableTagNames() {
        return UPDATABLE_TAG_NAMES;
    }

    @Override
    protected BuildingElement instanciateElement(long osmId) {
        return new BuildingElement(osmId);
    }

    @Override
    protected PssBuildingParser getParser() {
        return parser;
    }
    
    @Override
    protected AbstractMatcher<BuildingImport> getMatcher() {
        return this.matcher;
    }

    @Override
    protected AbstractMatchingScorer<BuildingElement> getScorer() {
      return this.scorer;
    }

    @Override
    protected String getChangesetSourceLabel() {
        return changesetSourceLabel;
    }

    @Override
    protected String getChangesetComment() {
        return changesetComment;
    }

    @Override
    protected float getMinimalMatchingScore() {
        return this.minMatchingScore;
    }
    
}

