package org.openstreetmap.osmaxil.plugin.parser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;

import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.model.xml.pss.PssBuilding;
import org.openstreetmap.osmaxil.model.xml.pss.PssRoot;
import org.openstreetmap.osmaxil.util.StringParsingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Repository;

@Repository
public class PssBuildingParser extends AbstractImportParser {

    @Autowired
    @Qualifier(value = "pssMarshaller")
    private Unmarshaller unmarshaller;

    int counter;
    
    PssRoot data;

    @Value("${plugins.pssBuildingParser.filePath}")
    private String filePath;

    @Value("${plugins.pssBuildingParser.srid}")
    private int srid;

    @PostConstruct
    public void init() throws IOException {
        FileInputStream is = null;
        BufferedInputStream bis = null;
        try {
            is = new FileInputStream(this.filePath);
            bis = new BufferedInputStream(is);
            this.data = (PssRoot) this.unmarshaller.unmarshal(new StreamSource(bis));
        } finally {
            if (is != null) {
                is.close();
                bis.close();
            }
        }
    }

    public boolean hasNext() {
        return counter < data.buildings.size();
    }

    public BuildingImport next() {
        BuildingImport result = null;
        if (counter >= data.buildings.size()) {
            return null;
        }
        PssBuilding building = data.buildings.get(counter++);
        result = parse(building);
        return result;
    }

    public void remove() {
        // TODO Auto-generated method stub
    }
    
    private BuildingImport parse(PssBuilding building) {
        BuildingImport result = new BuildingImport();
        result.setId(this.counter);
        String[] latlon = building.coordinates.split(",");
        if (latlon.length == 2) {
            result.setLat(StringParsingHelper.parseDouble(latlon[0], "latitude"));
            result.setLon(StringParsingHelper.parseDouble(latlon[1], "longitude"));
        }else {
            LOGGER.warn("Unable to parse latlon");
        }
        result.setHeight(StringParsingHelper.parseFloat(building.height, "height"));
        result.setUrl(building.url);
        return result;
    }
    
    public int getSrid() {
        return srid;
    }
    
}
