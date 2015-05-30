package org.openstreetmap.osmaxil.plugin.common.comparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.springframework.stereotype.Component;

@Component
public class TotalScoreComparator<Element extends AbstractElement> extends AbstractComparator<Element> {

    private Map<Long, Map<String, List<AbstractImport>>> importsByElementByTagValue;
    
    private Map<Long, Map<String, Float>> totalScoresByElementByTagValue;
    
    public TotalScoreComparator() {
        this.importsByElementByTagValue = new HashMap<Long, Map<String,List<AbstractImport>>>();
        this.totalScoresByElementByTagValue = new HashMap<Long, Map<String,Float>>();
    }    
    
    @Override
    public float computeElementMatchingScore(AbstractElement element, String matchingTagName) {
        this.dispatchMatchingImportsByTagValues(element, matchingTagName);
        this.computeTotalScoresByTagValues(element);  
        return this.getBestTotalScoreByElement(element.getOsmId());
    }
    
    @Override
    public String getBestTagValueByElement(long osmId) {
        String bestTagValue = null;
        Float bestTotalScore = null;
        Map<String, List<AbstractImport>> map = this.getMatchingImportsByElementByTagValue(osmId);
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
    
    private Map<String, List<AbstractImport>> getMatchingImportsByElementByTagValue(long osmId) {
        Map<String, List<AbstractImport>> result = this.importsByElementByTagValue.get(osmId);
        if (result == null) {
            result = new HashMap<String, List<AbstractImport>>();
            this.importsByElementByTagValue.put(osmId, result);
        }
        return result;
    }
    
    private Map<String, Float> getTotalScoresByElementByTagValue(long osmId) {
        Map<String, Float> result = this.totalScoresByElementByTagValue.get(osmId);
        if (result == null) {
            result = new HashMap<String, Float>();
            this.totalScoresByElementByTagValue.put(osmId, result);
        }
        return result;
    }
    
    private void dispatchMatchingImportsByTagValues(AbstractElement element, String matchingTagName) {
        LOGGER.info("Dispatch matching imports by their tag values for the tag " + matchingTagName);
        Map<String, List<AbstractImport>> map = this.getMatchingImportsByElementByTagValue(element.getOsmId());
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

    private void computeTotalScoresByTagValues(AbstractElement element) {
        LOGGER.info("Computing total scores by tag values");
        Map<String, List<AbstractImport>> map = this.getMatchingImportsByElementByTagValue(element.getOsmId());
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
            this.getTotalScoresByElementByTagValue(element.getOsmId()).put(updatableTagValue, score);
            LOGGER.info(" - for value=[" + updatableTagValue + "] total score is " + score + " (sum of  " + sb.toString()+ ")");
        }
    }
    
    private Float getBestTotalScoreByElement(long osmId) {
        Float bestTotalScore = null;
        for (Float totalScore : this.getTotalScoresByElementByTagValue(osmId).values()) {
            if (bestTotalScore == null || bestTotalScore < totalScore) {
                bestTotalScore = totalScore;
            }
        }
        return bestTotalScore;
    }
    
}
