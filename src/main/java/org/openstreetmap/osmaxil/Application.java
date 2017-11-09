package org.openstreetmap.osmaxil;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.flow.__AbstractImportFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component("OsmaxilApp")
public class Application {

	@Value("${osmaxil.flow}")
	public String flow;
	
	public static final String NAME = "Osmaxil";

	private static ClassPathXmlApplicationContext applicationContext;

	static private final Logger LOGGER = Logger.getLogger(Application.class);

	static protected final Logger LOGGER_FOR_STATS = Logger.getLogger("LoggerForStats");

	public static void main(String[] args) {
		applicationContext = new ClassPathXmlApplicationContext("spring.xml");
		Application app = (Application) applicationContext.getBean("OsmaxilApp");
		LOGGER.info("=== Starting Osmaxil ===");
		long startTime = System.currentTimeMillis();
		try {
			app.run();
		} catch (Exception e) {
			LOGGER.error(e);
		}
		LOGGER_FOR_STATS.info("Job has been executed in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
		LOGGER.info("=== Osmaxil has finished its job ===");
		applicationContext.close();
	}
	
	public void run() throws Exception {
		__AbstractImportFlow<?, ?> flow = (__AbstractImportFlow<?, ?>) applicationContext.getBean(this.flow);
		flow.load();
		flow.process();
		flow.synchronize();
		flow.displayLoadingStatistics();
		flow.displayProcessingStatistics();
	}
}
