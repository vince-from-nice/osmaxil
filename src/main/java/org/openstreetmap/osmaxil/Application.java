package org.openstreetmap.osmaxil;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.flow.__AbstractImportFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component("OsmaxilApp")
public class Application {

	@Value("${osmApi.flow}")
	public String flowName;
	
//	@Autowired
//	@Resource(name = "${osmApi.flow}")
//	protected __AbstractImportFlow<?, ?> flow;
	
	public static final String NAME = "Osmaxil";

	private static ClassPathXmlApplicationContext applicationContext;

	static private final Logger LOGGER = Logger.getLogger(Application.class);

	static protected final Logger LOGGER_FOR_STATS = Logger.getLogger("LoggerForStats");

	public static void main(String[] args) {
		//Application app = new Application();
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
	}

//	public void run() throws java.lang.Exception {
//		flow.load();
//		flow.process();
//		flow.synchronize();
//	}
	
	public void run() throws Exception {
		//this.applicationContext = new ClassPathXmlApplicationContext("spring.xml");

		//BuildingElevatorFlow plugin = (BuildingElevatorFlow) this.applicationContext.getBean("BuildingElevator");
		// PssBuildingUpdater plugin = (BuildingUpdater) this.applicationContext.getBean("BuildingUpdater");
		// ParisBuildingRemaker plugin = (BuildingRemaker) this.applicationContext.getBean("BuildingRemaker");
		//VegetationMakerFlow plugin = (VegetationMakerFlow) this.applicationContext.getBean("VegetationMaker");
		
		__AbstractImportFlow<?, ?> plugin = (__AbstractImportFlow<?, ?>) applicationContext.getBean(this.flowName);

		plugin.load();
		plugin.process();
		plugin.synchronize();

		plugin.displayLoadingStatistics();
		plugin.displayProcessingStatistics();
		// plugin.displaySynchronizingStatistics();

		this.applicationContext.close();
	}
}
