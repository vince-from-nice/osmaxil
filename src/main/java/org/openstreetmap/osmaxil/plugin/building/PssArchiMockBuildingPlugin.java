package org.openstreetmap.osmaxil.plugin.building;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.data.ElementTagNames;
import org.openstreetmap.osmaxil.data.building.BuildingImport;
import org.springframework.stereotype.Component;

@Component ("PssArchiMockPlugin")
public class PssArchiMockBuildingPlugin extends AbstractBuildingPlugin  {

    int counter;
    
    static List<BuildingImport> data;
    
    private float minimumMatchingScore;
    
    @PostConstruct
    public void init() {
        
        updatableTagNames.add(ElementTagNames.HEIGHT);
        updatableTagNames.add(ElementTagNames.BUILDING_LEVELS);
        
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
    public String getChangesetSource() {
        return "PSS (http://www.pss-archi.eu)";
    }
    
    @Override
    public float getMinMatchingScoreForUpdate() {
        return this.minimumMatchingScore;
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
