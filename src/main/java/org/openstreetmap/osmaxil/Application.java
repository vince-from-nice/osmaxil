package org.openstreetmap.osmaxil;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.service.ElementProcessor;
import org.openstreetmap.osmaxil.service.ElementUpdater;
import org.openstreetmap.osmaxil.service.ImportLoader;
import org.openstreetmap.osmaxil.service.StatsGenerator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
    
    private ClassPathXmlApplicationContext applicationContext;
    
    private ImportLoader importLoader;
    
    private ElementProcessor elementProcessor;
    
    //private ElementUpdater elementUpdater;
    
    private StatsGenerator statsGenerator;
    
    static private final Logger LOGGER = Logger.getLogger(ImportLoader.class);

    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);
    }
    
    public void run(String[] args) {
        LOGGER.info("=== Starting Osmaxil ===");
        this.applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        this.importLoader = this.applicationContext.getBean(ImportLoader.class);
        this.elementProcessor = this.applicationContext.getBean(ElementProcessor.class);
        //this.elementUpdater = this.applicationContext.getBean(ElementUpdater.class);
        this.statsGenerator = this.applicationContext.getBean(StatsGenerator.class);
        this.importLoader.loadImports();
        this.elementProcessor.processElements();
        //this.elementUpdater.updateElements();
        this.statsGenerator.generateStats();
        this.applicationContext.close();
        LOGGER.info("=== Osmaxil has finished its job ===");
    }
}
