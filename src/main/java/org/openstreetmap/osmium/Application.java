package org.openstreetmap.osmium;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.service.ImportService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
    
    private ClassPathXmlApplicationContext applicationContext;
    
    private ImportService enhancerService;
    
    static private final Logger LOGGER = Logger.getLogger(ImportService.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Osmium");
        Application app = new Application();
        app.run(args);
        LOGGER.info("=== Osmium has finished its job ===");
    }
    
    public Application() {
        //this.applicationContext = new AnnotationConfigApplicationContext(Application.class);
        this.applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        this.enhancerService = this.applicationContext.getBean(ImportService.class);
    }
    
    public void run(String[] args) {
        this.enhancerService.importBuildings();
        this.applicationContext.close();
    }
}
