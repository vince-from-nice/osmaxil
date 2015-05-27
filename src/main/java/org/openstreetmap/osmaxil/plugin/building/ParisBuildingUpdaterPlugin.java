package org.openstreetmap.osmaxil.plugin.building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTagNames;
import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.openstreetmap.osmaxil.plugin.AbstractUpdaterPlugin;
import org.openstreetmap.osmaxil.plugin.parser.ParisBuildingParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("ParisBuildingUpdaterPlugin")
public class ParisBuildingUpdaterPlugin extends AbstractUpdaterPlugin<BuildingElement, BuildingImport> {

    @Autowired
    private ParisBuildingParser parser;
    
    @Autowired
    private BuildingHelper helper;
    
    @Value("${plugins.parisBuildingUpdater.minMatchingScore}")
    private float minMatchingScore;

    @Value("${plugins.parisBuildingUpdater.changesetSourceLabel}")
    private String changesetSourceLabel;
    
    @Value("${plugins.parisBuildingUpdater.changesetComment}")
    private String changesetComment;
    
    private Map<Float, Map<String, List<AbstractImport>>> importsByTagValuesByElement;
    
    private Map<Float, Map<String, Float>> totalScoresByTagValuesByElement;
    
    private static final String UPDATABLE_TAG_NAMES[] = new String[] {ElementTagNames.BUILDING_LEVELS};
    
    private static final String MATCHING_TAG_NAME = ElementTagNames.BUILDING_LEVELS;
    
    public ParisBuildingUpdaterPlugin() {
        this.importsByTagValuesByElement = new HashMap<Float, Map<String,List<AbstractImport>>>();
        this.totalScoresByTagValuesByElement = new HashMap<Float, Map<String,Float>>();
    }    
    
    @Override
    public String[] getUpdatableTagNames() {
        return UPDATABLE_TAG_NAMES;
    }

    @Override
    public float computeElementMatchingScore(BuildingElement building) {
        this.dispatchMatchingImportsByTagValues(building, MATCHING_TAG_NAME);
        this.computeTotalScoresByTagValues(building, MATCHING_TAG_NAME);  
        return this.getBestTotalScoreByElement(building.getOsmId());
    }
    
    @Override
    public boolean isElementTagUpdatable(BuildingElement element, String tagName) {
        // Building tags are updatable only if it doesn't have an original value
        return element.getOriginalValuesByTagNames().get(tagName) == null;
    }
    
    @Override
    public boolean updateElementTag(BuildingElement element, String tagName) {
        String tagValue = this.getBestTagValueByElement(element.getOsmId());
        if (tagValue == null) {
            LOGGER.warn("Cannot update tag because best tag value is null for " + tagName);
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
       return this.helper.findMatchingBuildings(imp, this.getParser().getSrid());
    }

    @Override
    public float computeImportMatchingScore(BuildingImport imp) {
        return this.helper.computeMatchingScoreBasedOnBuildingArea(imp);
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
    
    private void dispatchMatchingImportsByTagValues(AbstractElement element, String matchingTagName) {
        Map<String, List<AbstractImport>> map = this.getMatchingImportsByTagValuesByElement(element.getOsmId());
        // For each matching import..
        for (AbstractImport imp : element.getMatchingImports()) {
            // Dispatch it by its tag value
            String updatableTagValue = imp.getTagValue(matchingTagName);
            // String updatableTagValue = element.getTagValue(updatableTagName);
            if (map.get(updatableTagValue) == null) {
                map.put(updatableTagValue, new ArrayList<AbstractImport>());
            }
            map.get(updatableTagValue).add(imp);
        }
    }

    private void computeTotalScoresByTagValues(AbstractElement element, String matchingTagName) {
        LOGGER.info("Computing total scores by values for the tag " + matchingTagName);
        Map<String, List<AbstractImport>> map = this.getMatchingImportsByTagValuesByElement(element.getOsmId());
        // For each tag value
        for (String updatableTagValue : map.keySet()) {
            // Compute matching score
            Float score = new Float(0);
            StringBuilder sb = new StringBuilder();
            for (Iterator<AbstractImport> iterator = map.get(updatableTagValue).iterator(); iterator.hasNext();) {
                AbstractImport imp = (AbstractImport) iterator.next();
                score += imp.getMatchingScore();
                sb.append("" + imp.getMatchingScore() + " (id=" + imp.getId() + ")");
                if (iterator.hasNext()) {
                    sb.append(" + ");
                }
            }
            this.getTotalScoresByTagValuesByElement(element.getOsmId()).put(updatableTagValue, score);
            LOGGER.info(" - for value=[" + updatableTagValue + "] total score is " + score + " (sum of  " + sb.toString()+ ")");
        }
    }
    
    private Map<String, List<AbstractImport>> getMatchingImportsByTagValuesByElement(float osmId) {
        Map<String, List<AbstractImport>> result = this.importsByTagValuesByElement.get(osmId);
        if (result == null) {
            result = new HashMap<String, List<AbstractImport>>();
            this.importsByTagValuesByElement.put(osmId, result);
        }
        return result;
    }
    
    private Map<String, Float> getTotalScoresByTagValuesByElement(float osmId) {
        Map<String, Float> result = this.totalScoresByTagValuesByElement.get(osmId);
        if (result == null) {
            result = new HashMap<String, Float>();
            this.totalScoresByTagValuesByElement.put(osmId, result);
        }
        return result;
    }
    
    private Float getBestTotalScoreByElement(float osmId) {
        Float bestTotalScore = null;
        for (Float totalScore : this.getTotalScoresByTagValuesByElement(osmId).values()) {
            if (bestTotalScore == null || bestTotalScore < totalScore) {
                bestTotalScore = totalScore;
            }
        }
        return bestTotalScore;
    }
    
    private String getBestTagValueByElement(float osmId) {
        String bestTagValue = null;
        Float bestTotalScore = null;
        Map<String, List<AbstractImport>> map = this.getMatchingImportsByTagValuesByElement(osmId);
        for (String tagValue : map.keySet()) {
            List<AbstractImport> importList = map.get(tagValue);
            // TODO use precalculated total scores
            float totalScore = 0;
            for (AbstractImport imp : importList) {
                totalScore += imp.getMatchingScore();
            }
            if (bestTotalScore == null || bestTotalScore < totalScore) {
                bestTotalScore = totalScore;
                bestTagValue = tagValue;
            }
        }
        return bestTagValue;
    }
    
}
