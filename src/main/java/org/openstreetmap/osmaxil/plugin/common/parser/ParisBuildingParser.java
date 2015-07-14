package org.openstreetmap.osmaxil.plugin.common.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.StringCoordinates;
import org.openstreetmap.osmaxil.model.building.BuildingImport;
import org.openstreetmap.osmaxil.util.StringParsingHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import au.com.bytecode.opencsv.CSVReader;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

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
    
    @Override
    public boolean hasNext() {
        // return this.reader.iterator().hasNext() // doesn't work ?
        return this.hasNext;
    }

    @Override
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
        BuildingImport building = new BuildingImport();
        building.setId(this.rowCount);
        String[] latlon = row[0].split(",");
        if (latlon.length == 2) {
            building.setLatitude(StringParsingHelper.parseDouble(latlon[0], "latitude"));
            building.setLongitude(StringParsingHelper.parseDouble(latlon[1], "longitude"));
        }else {
            LOGGER.warn("Unable to parse latlon");
        }
        if (row.length > 1) {
            String geom = row[1].substring(row[1].indexOf(GEOM_TOKEN) + GEOM_TOKEN.length());
            building.setGeometryRawString(geom.substring(0, geom.length() - 2));
            try {
                this.parseGeometry(building);
            } catch (Exception e) {
                LOGGER.error("Unable to build geometry: " + e.getMessage());
            }
        }
        if (row.length > 19) {
            building.setLevels(StringParsingHelper.parseInt(row[19], "levels"));
        } else {
            LOGGER.warn("Unable to parse levels");
        }
        if (row.length > 6) {
            building.setArea((int) StringParsingHelper.parseFloat(row[6], "area"));
        } else {
            LOGGER.warn("Unable to parse area");
        }        
        return building;
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
    }

    @Override
    public int getSrid() {
        return srid;
    }
    
    // TODO call it after the filtering (no need to spend time on filtered imports)
    private void parseGeometry(BuildingImport building) throws ParseException {
        String geom = building.getGeometryRawString();
        StringBuilder wktForPolygon = new StringBuilder("POLYGON(("); 
        geom = geom.replace("[", "").replace("]", "").replace(",", "");
        String[] coords = geom.split(" ");
        for (int i = 0; i < coords.length; i++) {
            if (i % 2 == 1) {
                String coordinates = coords[i-1] + " " + coords[i];
                wktForPolygon.append(coordinates);
                if (i < coords.length - 1) {
                    wktForPolygon.append(", ");
                }
                // Add a new point with string coordinates
                StringCoordinates sc = new StringCoordinates(coords[i - 1], coords[i], "0.0");
                building.getCoordinates().add(sc);
                // Add a new point with real geometry
                Point point = (Point) this.wktReader.read("POINT(" + coordinates + ")");
                building.getPoints().add(point);
            }
        }
        wktForPolygon.append("))");
        building.setGeometryAsWKT(wktForPolygon.toString());
        building.setPolygon((Polygon) wktReader.read(wktForPolygon.toString()));
    }

}
