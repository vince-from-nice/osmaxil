package org.openstreetmap.osmaxil.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.api.OsmApiRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Repository;

@Repository
public class OsmXmlFile {
    
    @Autowired
    private Marshaller marshaller;
    
    static private final Logger LOGGER = Logger.getLogger(Application.class);
    
    static private final String GEN_DIR = "gen";
    
    public boolean write(String name, OsmApiRoot root) {
        boolean result = false;
        String fileName = this.getFileName(name);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(fileName);
            this.marshaller.marshal(root, new StreamResult(os));
            result = true;
            LOGGER.info("File " + fileName + " has been created");
        } catch (IOException e) {
            LOGGER.error("Unable to write "  + fileName + ": " + e.getMessage());
            result = false;
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close "  + fileName + ": " + e.getMessage());
                    result = false;
                }
            }
        }
        return result;
    }
    
    private String getFileName(String name) {
        return GEN_DIR + File.separator + "genfile-" + name + ".osm.xml";
    }

}
