package org.openstreetmap.osmaxil.flow;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.BuildingImport;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.service.matcher.AbstractImportMatcher;
import org.openstreetmap.osmaxil.service.matcher.BuildingImportMatcher;
import org.openstreetmap.osmaxil.service.parser.PssBuildingImportParser;
import org.openstreetmap.osmaxil.service.selector.AbstractMatchingScoreSelector;
import org.openstreetmap.osmaxil.service.selector.ExclusiveMatchingScoreSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component ("BuildingUpdaterForPSS") @Lazy
public class BuildingUpdaterFlowForPSS extends AbstractUpdaterFlow<BuildingElement, BuildingImport> {

    @Autowired
    private PssBuildingImportParser parser;

    @Autowired
    private BuildingImportMatcher matcher;
    
    @Autowired
    private ExclusiveMatchingScoreSelector<BuildingElement> scorer;
    
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
    
    // =========================================================================
    // Private methods
    // =========================================================================

    @PostConstruct
    public void init() {
        this.matcher.setWithSurfaces(false); 
    }
    
}

