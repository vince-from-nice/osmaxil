package org.openstreetmap.osmaxil.plugin.building;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.data.ElementTagNames;
import org.openstreetmap.osmaxil.data.building.BuildingImport;
import org.openstreetmap.osmaxil.util.StringParsingHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

@Component("OpenDataParisBuildingPlugin")
public class OpenDataParisBuildingPlugin extends AbstractBuildingPlugin {

    private CSVReader reader;

    long rowCount;

    boolean hasNext;

    @Value("${plugins.openDataParis.updatableTagNames}")
    private String updatableTagNames;
    
    @Value("${plugins.openDataParis.changesetSourceLabel}")
    private String changesetSourceLabel;
    
    @Value("${plugins.openDataParis.changesetComment}")
    private String changesetComment;
    
    @Value("${plugins.openDataParis.minMatchingScore}")
    private float minMatchingScore;
    
    @Value("${plugins.openDataParis.filePath}")
    private String csvFilePath;

    @PostConstruct
    public void init() throws FileNotFoundException {
        LOGGER.info("Init of OpenDataParisCsvFileLoader");
        InputStreamReader isr = new InputStreamReader(new FileInputStream(this.csvFilePath));
        this.reader = new CSVReader(new BufferedReader(isr), (char) ';', (char) '\'', 1);
        // this.rows = reader.readAll();
        this.hasNext = true;
    }
    
    @Override
    public String[] getUpdatableTagNames() {
        return updatableTagNames.split(",");
    }
    
    @Override
    public float getMinMatchingScoreForUpdate() {
        return this.minMatchingScore;
    }
    
    @Override
    public String getChangesetComment() {
        return this.changesetComment ;
    }
    
    @Override
    public String getChangesetSourceLabel() {
        return this.changesetSourceLabel;
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
    
}
