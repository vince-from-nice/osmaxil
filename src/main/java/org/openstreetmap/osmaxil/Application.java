package org.openstreetmap.osmaxil;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.flow.BuildingElevatorFlow;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
    
    public static final String NAME = "Osmaxil";
    
    private ClassPathXmlApplicationContext applicationContext;
    
    static private final Logger LOGGER = Logger.getLogger(Application.class);
    
    static protected final Logger LOGGER_FOR_STATS = Logger.getLogger("LoggerForStats");

    public static void main(String[] args) {
        Application app = new Application();
        LOGGER.info("=== Starting Osmaxil ===");
        long startTime = System.currentTimeMillis();
        app.run();
        LOGGER_FOR_STATS .info("Job has been executed in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        LOGGER.info("=== Osmaxil has finished its job ===");
    }

    public void run() {
        this.applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        
        BuildingElevatorFlow plugin = (BuildingElevatorFlow) this.applicationContext.getBean("BuildingEnhancer");        
        //PssBuildingUpdater plugin = (BuildingUpdater) this.applicationContext.getBean("BuildingUpdater");
        //ParisBuildingRemaker plugin = (BuildingRemaker) this.applicationContext.getBean("BuildingRemaker");
        //NiceTreeMaker plugin = (TreeMaker) this.applicationContext.getBean("TreeMaker");
        
        plugin.prepare();
        plugin.load();
        //plugin.process();
        //plugin.synchronize();
        
        plugin.displayLoadingStatistics();
        plugin.displayProcessingStatistics();
        plugin.displaySynchronizingStatistics();
        
        this.applicationContext.close();
    }
}
