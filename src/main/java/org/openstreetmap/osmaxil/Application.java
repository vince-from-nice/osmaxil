package org.openstreetmap.osmaxil;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.service.ElementProcessor;
import org.openstreetmap.osmaxil.service.ElementSynchronizer;
import org.openstreetmap.osmaxil.service.ImportLoader;
import org.openstreetmap.osmaxil.service.StatsGenerator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
    
    private ClassPathXmlApplicationContext applicationContext;
    
    private ImportLoader importLoader;
    
    private ElementProcessor elementProcessor;
    
    private ElementSynchronizer elementSynchronizer;
    
    private StatsGenerator statsGenerator;
    
    static private final Logger LOGGER = Logger.getLogger(ImportLoader.class);

    public static void main(String[] args) {
        Application app = new Application();
        LOGGER.info("=== Starting Osmaxil ===");
        app.init(args);
        app.run();
        LOGGER.info("=== Osmaxil has finished its job ===");
    }
    
    public void init(String[] args) {
        // TODO Bootstrap Spring beans correctly
        this.applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        this.importLoader = this.applicationContext.getBean(ImportLoader.class);
        this.elementProcessor = this.applicationContext.getBean(ElementProcessor.class);
        this.elementSynchronizer = this.applicationContext.getBean(ElementSynchronizer.class);
        this.statsGenerator = this.applicationContext.getBean(StatsGenerator.class);
    }
    
    public void run() {
        this.importLoader.loadImports();
        this.elementProcessor.processElements();
        this.elementSynchronizer.synchronizeElements();
        this.statsGenerator.generateStats();
        this.applicationContext.close();
        
    }
}
