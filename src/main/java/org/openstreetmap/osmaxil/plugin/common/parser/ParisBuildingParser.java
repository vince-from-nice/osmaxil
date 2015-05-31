package org.openstreetmap.osmaxil.plugin.common.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.util.StringParsingHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import au.com.bytecode.opencsv.CSVReader;

@Repository
public class ParisBuildingParser extends AbstractParser<BuildingImport> {
    
    private CSVReader reader;

    long rowCount;

    boolean hasNext;
    
    @Value("${plugins.parisBuildingParser.filePath}")
    private String filePath;
    
    @Value("${plugins.parisBuildingParser.srid}")
    private int srid;
    
    static private final String GEOM_TOKEN = "\"\"coordinates\"\": ";
    
    @PostConstruct
    public void init() throws FileNotFoundException {
        LOGGER.info("Init of OpenDataParisCsvFileLoader");
        InputStreamReader isr = new InputStreamReader(new FileInputStream(this.filePath));
        this.reader = new CSVReader(new BufferedReader(isr), (char) ';', (char) '\'', 1);
        // this.rows = reader.readAll();
        this.hasNext = true;
    }
    
    public boolean hasNext() {
        // return this.reader.iterator().hasNext() // doesn't work ?
        return this.hasNext;
    }

    public BuildingImport next() {
        String[] row = null;
        try {
            row = this.reader.readNext();
        } catch (IOException e) {
            LOGGER.info("An error has occured while reading CSV: " + e.getMessage());
        }
        if (row == null || row[0] == null) {
            this.hasNext = false;
            return null;
        }
        this.rowCount++;
        BuildingImport result = new BuildingImport();
        result.setId(this.rowCount);
        String[] latlon = row[0].split(",");
        if (latlon.length == 2) {
            result.setLat(StringParsingHelper.parseDouble(latlon[0], "latitude"));
            result.setLon(StringParsingHelper.parseDouble(latlon[1], "longitude"));
        }else {
            LOGGER.warn("Unable to parse latlon");
        }
        if (row.length > 1) {
            String geom = row[1].substring(row[1].indexOf(GEOM_TOKEN) + GEOM_TOKEN.length());
            result.setGeometry(geom.substring(0, geom.length() - 2));
        }
        if (row.length > 19) {
            result.setLevels(StringParsingHelper.parseInt(row[19], "levels"));
        } else {
            LOGGER.warn("Unable to parse levels");
        }
        if (row.length > 6) {
            result.setArea((int) StringParsingHelper.parseFloat(row[6], "area"));
        } else {
            LOGGER.warn("Unable to parse area");
        }        
        return result;
    }

    public void remove() {
        // TODO Auto-generated method stub
    }

    public int getSrid() {
        return srid;
    }

}
