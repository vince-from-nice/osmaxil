package org.openstreetmap.osmium.plugin.building;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmium.data.BuildingImport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import au.com.bytecode.opencsv.CSVReader;

@Repository("OpenDataParisCsvPlugin")
public class OpenDataParisBuildingPlugin extends AbstractBuildingPlugin {

    private CSVReader reader;

    // List<String[]> rows;

    long rowCount;

    boolean hasNext;

    @Value("${plugins.openDataParis.filePath}")
    private String csvFilePath;

    @PostConstruct
    public void init() throws FileNotFoundException {
        LOGGER.info("Init of OpenDataParisCsvFileLoader");
        this.reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(this.csvFilePath))),
                (char) ';', (char) '\'', 1);
        // this.rows = reader.readAll();
        this.hasNext = true;
    }

    @Override
    public String getChangesetSource() {
        return "ParisData (http://opendata.paris.fr)";
    }
    
    public boolean hasNext() {
        // return counter < rows.size() // we don't pre load all lines anymore
        // return this.reader.iterator().hasNext() // doesn't work ?
        return this.hasNext;
    }

    public BuildingImport next() {
        // if (counter < rows.size()) return rows.get(counter++)
        // return null;
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
            result.setLat(this.parseDouble(latlon[0], "latitude"));
            result.setLon(this.parseDouble(latlon[1], "longitude"));
        }else {
            LOGGER.warn("Unable to parse latlon");
        }
        if (row.length >= 19) {
            result.setLevels(this.parseInt(row[19], "levels"));
        } else {
            LOGGER.warn("Unable to parse levels");
        }
        if (row.length >= 6) {
            result.setArea((int) this.parseFloat(row[6], "area"));
        } else {
            LOGGER.warn("Unable to parse area");
        }        
        return result;
    }

    public void remove() {
        // TODO Auto-generated method stub
    }
    
    // Stupid but useful methods for catching and logging exception individually 
    
    protected double parseDouble(String s, String name) {
        double result = 0;
        try {
            result = Double.parseDouble(s);
        } catch (Exception e) {
            LOGGER.warn("Unable to parse " + name + ": " + e.getMessage());
        }
        return result;
    }

    protected int parseInt(String s, String name) {
        int result = 0;
        try {
            result = Integer.parseInt(s);
        } catch (Exception e) {
            LOGGER.warn("Unable to parse " + name + ": " + e.getMessage());
        }
        return result;
    }

    protected double parseFloat(String s, String name) {
        float result = 0;
        try {
            result = Float.parseFloat(s);
        } catch (Exception e) {
            LOGGER.warn("Unable to parse " + name + ": " + e.getMessage());
        }
        return result;
    }
}
