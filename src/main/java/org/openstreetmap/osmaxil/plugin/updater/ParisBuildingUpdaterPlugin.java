package org.openstreetmap.osmaxil.plugin.updater;

import java.util.List;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTagNames;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.plugin.common.comparator.CumulativeMatchingImportComparator;
import org.openstreetmap.osmaxil.plugin.common.matcher.BuildingMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.ParisBuildingParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("ParisBuildingUpdaterPlugin")
public class ParisBuildingUpdaterPlugin extends AbstractUpdaterPlugin<BuildingElement, BuildingImport> {

    @Autowired
    private ParisBuildingParser parser;
    
    @Autowired
    private BuildingMatcher matcher;
    
    @Autowired
    private CumulativeMatchingImportComparator<BuildingElement> comparator;
    
    @Value("${plugins.parisBuildingUpdater.minMatchingScore}")
    private float minMatchingScore;

    @Value("${plugins.parisBuildingUpdater.changesetSourceLabel}")
    private String changesetSourceLabel;
    
    @Value("${plugins.parisBuildingUpdater.changesetComment}")
    private String changesetComment;
    
    private static final String UPDATABLE_TAG_NAMES[] = new String[] {ElementTagNames.BUILDING_LEVELS};
    
    private static final String MATCHING_TAG_NAME = ElementTagNames.BUILDING_LEVELS;
    
    @PostConstruct
    public void init() {
        this.comparator.setMatchingTagName(MATCHING_TAG_NAME);
    }
    
    @Override
    public String[] getUpdatableTagNames() {
        return UPDATABLE_TAG_NAMES;
    }

    @Override
    public float computeElementMatchingScore(BuildingElement building) {
        return this.comparator.computeElementMatchingScore(building);
    }
    
    @Override
    public boolean isElementTagUpdatable(BuildingElement element, String tagName) {
        // Building tags are updatable only if it doesn't have an original value
        return element.getOriginalValuesByTagNames().get(tagName) == null;
    }
    
    @Override
    public boolean updateElementTag(BuildingElement element, String tagName) {
        AbstractImport bestImport = this.comparator.getBestMatchingImportByElement(element);
        String tagValue = bestImport.getTagValue(tagName);
        if (tagValue == null) {
            LOGGER.warn("Cannot update tag because best import tag value is null for " + tagName);
            return false;
        }
        boolean updated = false;
        if (ElementTagNames.HEIGHT.equals(tagName)) {
            // Adding +1 to levels because OSM use the US way to count building levels
            element.setLevels(Integer.parseInt(tagValue) + 1);
            LOGGER.info("===> Updating levels to " + (tagValue + 1));
            updated = true;
        }
        return updated;
    }
    
    @Override
    public List<MatchingElementId> findMatchingElements(BuildingImport imp) {
       return this.matcher.findMatchingImport(imp, this.getParser().getSrid());
    }

    @Override
    public float computeImportMatchingScore(BuildingImport imp) {
        return this.matcher.computeMatchingScore(imp);
    }

    @Override
    public BuildingElement instanciateElement(long osmId) {
        return new BuildingElement(osmId);
    }

    @Override
    public ParisBuildingParser getParser() {
        return parser;
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
