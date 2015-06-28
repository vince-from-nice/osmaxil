package org.openstreetmap.osmaxil.plugin.common.parser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;

import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.model.xml.pss.PssXmlBuilding;
import org.openstreetmap.osmaxil.model.xml.pss.PssXmlRoot;
import org.openstreetmap.osmaxil.util.StringParsingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Repository;

@Repository
public class PssBuildingParser extends AbstractParser<BuildingImport> {

    @Autowired
    @Qualifier(value = "pssMarshaller")
    private Unmarshaller unmarshaller;

    int counter;
    
    PssXmlRoot data;

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
            this.data = (PssXmlRoot) this.unmarshaller.unmarshal(new StreamSource(bis));
        } finally {
            if (is != null) {
                is.close();
                bis.close();
            }
        }
    }

    @Override
    public boolean hasNext() {
        return counter < data.buildings.size();
    }

    @Override
    public BuildingImport next() {
        BuildingImport result = null;
        if (counter >= data.buildings.size()) {
            return null;
        }
        PssXmlBuilding building = data.buildings.get(counter++);
        result = new BuildingImport();
        result.setId(this.counter);
        result.setName(building.name);
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

    @Override
    public void remove() {
        // TODO Auto-generated method stub
    }
    
    @Override
    public int getSrid() {
        return srid;
    }
    
}
