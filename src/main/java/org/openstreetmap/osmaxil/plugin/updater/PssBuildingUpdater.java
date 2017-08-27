package org.openstreetmap.osmaxil.plugin.updater;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.BuildingImport;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractImportMatcher;
import org.openstreetmap.osmaxil.plugin.common.matcher.BuildingImportMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.PssBuildingImportParser;
import org.openstreetmap.osmaxil.plugin.common.selector.AbstractMatchingScoreSelector;
import org.openstreetmap.osmaxil.plugin.common.selector.ExclusiveMatchingScoreSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component ("PssBuildingUpdater") @Lazy
public class PssBuildingUpdater extends AbstractUpdaterPlugin<BuildingElement, BuildingImport> {

    @Autowired
    private PssBuildingImportParser parser;

    @Autowired
    private BuildingImportMatcher matcher;
    
    @Autowired
    private ExclusiveMatchingScoreSelector<BuildingElement> scorer;
    
    @Value("${plugins.pssBuildingUpdater.minMatchingScore}")
    private float minMatchingScore;

    @Value("${plugins.pssBuildingUpdater.changesetSourceLabel}")
    private String changesetSourceLabel;
    
    @Value("${plugins.pssBuildingUpdater.changesetComment}")
    private String changesetComment;
    
    private int counterForFakeNames = 0;
            
    private static final String UPDATABLE_TAG_NAMES[] = new String[] {ElementTag.HEIGHT, ElementTag.NAME, ElementTag.URL};
    
    // =========================================================================
    // Overrided methods
    // =========================================================================
    
    @Override
    protected boolean isElementTagUpdatable(BuildingElement element, String tagName) {
        // Building tags are updatable only if it doesn't have an original value, except for the tag url which can have multiples values
        return (ElementTag.URL.equals(tagName) ? true : element.getOriginalValuesByTagNames().get(tagName) == null);
    }

    @Override
    protected boolean updateElementTag(BuildingElement element, String tagName) {
        String tagValue = element.getMatchingImports().get(0).getValueByTagName(tagName);
        if (tagValue == null) {
            LOGGER.warn("Cannot update tag because tag value is null for " + tagName);
            return false;
        }
        boolean updated = false;
        if (ElementTag.HEIGHT.equals(tagName)) {
            element.setHeight(Integer.parseInt(tagValue));
            LOGGER.info("===> Updating height to [" + tagValue + "]");
            updated = true;
        }
        if (ElementTag.URL.equals(tagName)) {
            element.setTagValue(ElementTag.URL, tagValue);
            LOGGER.info("===> Updating URL to [" + tagValue + "]");
            updated = true;
        }
        if (ElementTag.NAME.equals(tagName)) {
            // Ignore fake name based on the building address
           if (Character.isDigit(tagValue.charAt(0))) {
                LOGGER.info("Skipping name update because the value looks fake (" + tagValue + ")");
                this.counterForFakeNames++;
            } else {
                element.setTagValue(ElementTag.NAME, tagValue);
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
    protected PssBuildingImportParser getParser() {
        return parser;
    }
    
    @Override
    protected AbstractImportMatcher<BuildingImport> getMatcher() {
        return this.matcher;
    }

    @Override
    protected AbstractMatchingScoreSelector<BuildingElement> getScorer() {
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
    
    // =========================================================================
    // Private methods
    // =========================================================================

    @PostConstruct
    public void init() {
        this.matcher.setWithSurfaces(false); 
    }
    
}

