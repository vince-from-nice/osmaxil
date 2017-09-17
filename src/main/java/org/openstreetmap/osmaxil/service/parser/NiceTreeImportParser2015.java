package org.openstreetmap.osmaxil.service.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.TreeImport;
import org.openstreetmap.osmaxil.util.StringParsingHelper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import au.com.bytecode.opencsv.CSVReader;

@Repository @Lazy
public class NiceTreeImportParser2015 extends AbstractImportParser<TreeImport> {

    private CSVReader reader;

    long rowCount;

    boolean hasNext;
    
    static private final String GEOM_TOKEN = "\"coordinates\": [";
   
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
        //tree.setGenus(row[1]);
        //tree.setSpecies(row[2]);
        tree.setReference(row[0]);
        String geom = row[3].substring(row[3].indexOf(GEOM_TOKEN) + GEOM_TOKEN.length());
        geom = geom.substring(0, geom.length() - 2);
        String[] coords = geom.split(", ");
        tree.setLongitude(StringParsingHelper.parseDouble(coords[0], "lon"));
        tree.setLatitude(StringParsingHelper.parseDouble(coords[1], "lat"));
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
