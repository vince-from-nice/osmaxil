package org.openstreetmap.osmaxil;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.step.LoadingStep;
import org.openstreetmap.osmaxil.step.ProcessingStep;
import org.openstreetmap.osmaxil.step.StatisticsStep;
import org.openstreetmap.osmaxil.step.SynchronizingStep;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
    
    public static final String NAME = "Osmaxil";
    
    private ClassPathXmlApplicationContext applicationContext;
    
    private LoadingStep loadingStep;
    
    private ProcessingStep processingStep;
    
    private SynchronizingStep synchronizingStep;
    
    private StatisticsStep statisticsStep;
    
    static private final Logger LOGGER = Logger.getLogger(Application.class);

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
        this.loadingStep = this.applicationContext.getBean(LoadingStep.class);
        this.processingStep = this.applicationContext.getBean(ProcessingStep.class);
        this.synchronizingStep = this.applicationContext.getBean(SynchronizingStep.class);
        this.statisticsStep = this.applicationContext.getBean(StatisticsStep.class);
    }
    
    public void run() {
        this.loadingStep.loadImports();
        this.processingStep.processElements();
        this.synchronizingStep.synchronize();
        this.statisticsStep.generateStats();
        this.applicationContext.close();
    }
}
