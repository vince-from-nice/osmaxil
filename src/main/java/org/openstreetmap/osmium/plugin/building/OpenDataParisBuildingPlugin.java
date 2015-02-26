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

@Repository ("OpenDataParisCsvPlugin")
public class OpenDataParisBuildingPlugin extends AbstractBuildingPlugin  {

    private CSVReader reader;
    
    //List<String[]> rows;
    
    long rowCount;
    
    boolean hasNext;
    
    @Value ("${plugins.openDataParis.filePath}")
    private String csvFilePath;
    
    @PostConstruct
    public void init() throws FileNotFoundException {
        LOGGER.info("Init of OpenDataParisCsvFileLoader");
        this.reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(this.csvFilePath))), (char) ';', (char) '\'', 1);
        //this.rows = reader.readAll();
        this.hasNext = true;        
    }
    
    public boolean hasNext() {
        //return counter < rows.size() // we don't pre load all lines anymore
        //return this.reader.iterator().hasNext() // doesn't work ?
        return this.hasNext;
    }

    public BuildingImport next() {
//      if (counter < rows.size()) return rows.get(counter++)
//      return null;
        String [] row = null;
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
        result.setLon(Double.parseDouble(latlon[0]));
        result.setLat(Double.parseDouble(latlon[1]));
        result.setLevels(Integer.parseInt(row[19]));
        result.setArea((int) Float.parseFloat(row[6]));
        return result;
    }

    public void remove() {
        // TODO Auto-generated method stub
    }

}
