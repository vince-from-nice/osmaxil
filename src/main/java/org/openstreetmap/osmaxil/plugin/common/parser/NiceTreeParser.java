package org.openstreetmap.osmaxil.plugin.common.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.model.tree.TreeImport;
import org.springframework.beans.factory.annotation.Value;

import au.com.bytecode.opencsv.CSVReader;

public class NiceTreeParser extends AbstractParser<TreeImport> {

    private CSVReader reader;

    long rowCount;

    boolean hasNext;
    
    @Value("${plugins.parisBuildingParser.filePath}")
    private String filePath;
    
    @Value("${plugins.parisBuildingParser.srid}")
    private int srid;
    
    @PostConstruct
    public void init() throws FileNotFoundException {
        LOGGER.info("Init of OpenDataParisCsvFileLoader");
        InputStreamReader isr = new InputStreamReader(new FileInputStream(this.filePath));
        this.reader = new CSVReader(new BufferedReader(isr), (char) ';', (char) '\'', 1);
        // this.rows = reader.readAll();
        this.hasNext = true;
    }
    
    @Override
    public boolean hasNext() {
        // return this.reader.iterator().hasNext() // doesn't work ?
        return this.hasNext;
    }
    
    @Override
    public TreeImport next() {
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
        TreeImport tree = new TreeImport();
        tree.setId(this.rowCount);
        
        return tree;
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
