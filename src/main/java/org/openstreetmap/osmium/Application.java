package org.openstreetmap.osmium;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.service.ElementCache;
import org.openstreetmap.osmium.service.ImportLoader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
    
    private ClassPathXmlApplicationContext applicationContext;
    
    private ImportLoader importLoader;
    
    private ElementCache elementCache;
    
    static private final Logger LOGGER = Logger.getLogger(ImportLoader.class);

    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);
    }
    
    public void run(String[] args) {
        LOGGER.info("=== Starting Osmium ===");
        this.applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        this.importLoader = this.applicationContext.getBean(ImportLoader.class);
        this.elementCache = this.applicationContext.getBean(ElementCache.class);
        this.importLoader.loadImports();
        this.elementCache.processElements();
        this.applicationContext.close();
        LOGGER.info("=== Osmium has finished its job ===");
    }
}
