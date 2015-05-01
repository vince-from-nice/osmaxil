//package org.openstreetmap.osmaxil.plugin.building;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.annotation.PostConstruct;
//
//import org.openstreetmap.osmaxil.model.building.BuildingImport;
//import org.openstreetmap.osmaxil.plugin.loader.AbstractImportLoader;
//import org.openstreetmap.osmaxil.plugin.loader.ParisDataCsvBuildingLoader;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component ("PssBuildingUpdaterPlugin")
//public class PssBuildingUpdaterPlugin extends BuildingHelper  {
//
//    
//    @Value("${plugins.pssBuildingUpdater.updatableTagNames}")
//    private String updatableTagNames;
//    
//    @Value("${plugins.pssBuildingUpdater.changesetSourceLabel}")
//    private String changesetSourceLabel;
//    
//    @Value("${plugins.pssBuildingUpdater.changesetComment}")
//    private String changesetComment;
//    
//    @Value("${plugins.pssBuildingUpdater.minMatchingScore}")
//    private float minMatchingScore;
//    
//    @Value("${plugins.pssBuildingUpdater.filePath}")
//    private String csvFilePath;
//    
//    private AbstractImportLoader loader = new ParisDataCsvBuildingLoader();
//    
//    @Override
//    public AbstractImportLoader getLoader() {
//        return this.loader;
//    }
//
//    @Override
//    public String[] getUpdatableTagNames() {
//        return updatableTagNames.split(",");
//    }
//    
//    @Override
//    public String getChangesetSourceLabel() {
//        return changesetSourceLabel;
//    }
//
//    @Override
//    public String getChangesetComment() {
//        return changesetComment;
//    }
//    
//    @Override
//    public float getMinMatchingScoreForUpdate() {
//        return minMatchingScore;
//    }
//
//}
