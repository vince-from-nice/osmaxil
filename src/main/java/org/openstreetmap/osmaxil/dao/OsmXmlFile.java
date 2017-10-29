package org.openstreetmap.osmaxil.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.dao.xml.osm.OsmXmlRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Repository;

@Repository
public class OsmXmlFile {

	@Autowired
	@Qualifier(value = "osmMarshaller")
	private Marshaller marshaller;

	private int counterForWriteSuccess;

	private int counterForWriteFailures;

	static private final Logger LOGGER = Logger.getLogger(Application.class);

	static private final Logger LOGGER_FOR_STATS = Logger.getLogger("LoggerForStats");

	static private final String GEN_DIR = "gen";

	@PreDestroy
	public void close() {
		LOGGER_FOR_STATS.info("=== Closing OSM XML service ===");
		LOGGER_FOR_STATS.info("Total of writing successes: " + this.counterForWriteSuccess);
		LOGGER_FOR_STATS.info("Total of writing failures: " + this.counterForWriteFailures);
	}

	public boolean writeToFile(String name, OsmXmlRoot root) {
		boolean result = false;
		String fileName = null;
		FileOutputStream os = null;
		try {
			root.version = 0.6f;
			root.generator = Application.NAME;
			fileName = this.getFileName(name);
			os = null;
			os = new FileOutputStream(fileName);
			this.marshaller.marshal(root, new StreamResult(os));
			this.counterForWriteSuccess++;
			result = true;
			LOGGER.info("File " + fileName + " has been created");
		} catch (Exception e) {
			LOGGER.error("Unable to write file <" + fileName + ">: " + e.getMessage());
			this.counterForWriteFailures++;
			result = false;
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					LOGGER.error("Unable to close " + fileName + ": " + e.getMessage());
					this.counterForWriteFailures++;
					result = false;
				}
			}
		}
		return result;
	}

	private String getFileName(String name) {
		// TODO use subdirectories (based on an execution context ?)
		return GEN_DIR + File.separator + name + ".osm.xml";
	}

}
