package org.openstreetmap.osmaxil.plugin.building;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.data.building.BuildingImport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component ("PssArchiMockPlugin")
public class PssArchiMockBuildingPlugin extends AbstractBuildingPlugin  {

    int counter;
    
    static List<BuildingImport> data;
    
    @Value("${plugins.pssArchi.updatableTagNames}")
    private String updatableTagNames;
    
    @Value("${plugins.pssArchi.changesetSourceLabel}")
    private String changesetSourceLabel;
    
    @Value("${plugins.pssArchi.changesetComment}")
    private String changesetComment;
    
    @Value("${plugins.pssArchi.minMatchingScore}")
    private float minMatchingScore;
    
    @Value("${plugins.pssArchi.filePath}")
    private String csvFilePath;
    
    @PostConstruct
    public void init() {
        data = new ArrayList<BuildingImport>();
        BuildingImport b;
        
        b = new BuildingImport();
        b.setId(23478);
        b.setLat(43.5337008113957700);
        b.setLon(6.9355890154838560);
        b.setLevels(17);
        b.setName("Le Surcouf");
        data.add(b);
        
        b = new BuildingImport();
        b.setId(23477);
        b.setLat(43.5357619666915240);
        b.setLon(6.9355407357215880);
        b.setLevels(16);
        b.setName("Le Concorde");
        data.add(b);
    }
    
    @Override
    public String[] getUpdatableTagNames() {
        return updatableTagNames.split(",");
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
    public float getMinMatchingScoreForUpdate() {
        return minMatchingScore;
    }
    
    public boolean hasNext() {
        return counter < data.size();
    }

    public BuildingImport next() {
        if (counter < data.size()) return data.get(counter++);
        return null;
    }

    public void remove() {
        // TODO Auto-generated method stub
    }

}
