package org.openstreetmap.osmium;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.service.ElementMatcher;
import org.openstreetmap.osmium.service.ElementUpdater;
import org.openstreetmap.osmium.service.ImportLoader;
import org.openstreetmap.osmium.service.StatsGenerator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
    
    private ClassPathXmlApplicationContext applicationContext;
    
    private ImportLoader importLoader;
    
    private ElementMatcher elementMatcher;
    
    private ElementUpdater elementUpdater;
    
    private StatsGenerator statsGenerator;
    
    static private final Logger LOGGER = Logger.getLogger(ImportLoader.class);

    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);
    }
    
    public void run(String[] args) {
        LOGGER.info("=== Starting Osmium ===");
        this.applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        this.importLoader = this.applicationContext.getBean(ImportLoader.class);
        this.elementMatcher = this.applicationContext.getBean(ElementMatcher.class);
        this.elementUpdater = this.applicationContext.getBean(ElementUpdater.class);
        this.statsGenerator = this.applicationContext.getBean(StatsGenerator.class);
        this.importLoader.loadImports();
        this.elementMatcher.processElements();
        //this.elementUpdater.updateElements();
        this.statsGenerator.displayStats();
        this.applicationContext.close();
        LOGGER.info("=== Osmium has finished its job ===");
    }
}
