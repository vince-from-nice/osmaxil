package org.openstreetmap.osmaxil.plugin.building;

import java.util.List;

import org.openstreetmap.osmaxil.model.MatchingElementId;
import org.openstreetmap.osmaxil.model.building.BuildingElement;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.plugin.AbstracMakerPlugin;
import org.openstreetmap.osmaxil.plugin.loader.ParisDataCsvBuildingLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class ParisBuildingMakerPlugin extends AbstracMakerPlugin<BuildingElement, BuildingImport> {
    
    @Autowired
    private ParisDataCsvBuildingLoader loader;
    
    @Autowired
    private BuildingHelper helper;

    @Value("${plugins.parisBuildingMaker.changesetSourceLabel}")
    private String changesetSourceLabel;
    
    @Value("${plugins.parisBuildingMaker.changesetComment}")
    private String changesetComment;

    @Override
    public List<MatchingElementId> findMatchingElements(BuildingImport imp) {
       return this.helper.findMatchingBuildings(imp);
    }

    @Override
    public float computeMatchingScore(BuildingImport imp) {
        return this.helper.computeMatchingScore(imp);
    }

    @Override
    public BuildingElement instanciateElement(long osmId) {
        return new BuildingElement(osmId);
    }

    public ParisDataCsvBuildingLoader getLoader() {
        return loader;
    }

    public void setLoader(ParisDataCsvBuildingLoader loader) {
        this.loader = loader;
    }

    public String getChangesetSourceLabel() {
        return changesetSourceLabel;
    }

    public void setChangesetSourceLabel(String changesetSourceLabel) {
        this.changesetSourceLabel = changesetSourceLabel;
    }

    public String getChangesetComment() {
        return changesetComment;
    }

    public void setChangesetComment(String changesetComment) {
        this.changesetComment = changesetComment;
    }
    
}
