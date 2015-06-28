package org.openstreetmap.osmaxil.step;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.dao.ElementStore;
import org.openstreetmap.osmaxil.dao.OsmXml;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.plugin.remaker.AbstractRemakerPlugin;
import org.openstreetmap.osmaxil.plugin.updater.AbstractUpdaterPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SynchronizingStep extends AbstractStep {

    private long counterForMatchedElements;

    private long counterForAlteredElements;

    @Autowired
    private ElementStore elementCache;
    
    @Value("${osmaxil.syncMode}")
    private String synchronizationMode;
    
    @Autowired
    private OsmXml osmXmlFile;

    //@PostConstruct
    public void init() {
        // Need to do an init on demand, when the synchronization phase is going to start to write elements.
        // If it would have been done on the Spring context initialization the first changeset could have become obsolete 
        // because changeset has an idle timeout of 1h and the previous phase (imports loading) could have taken more time.
        this.osmApiService.initForWriting(this.plugin.getChangesetSourceLabel(), this.plugin.getChangesetComment());
        this.synchronizationMode = this.synchronizationMode.trim();
    }
    
    @Override
    public void displayStats() {
        LOGGER_FOR_STATS.info("=== Statistics for " + this.getClass().getSimpleName() + "===");
        LOGGER.info("Total of matched elements: " + this.counterForMatchedElements);
        LOGGER.info("Total of altered elements: " + this.counterForAlteredElements);
    }

    public void synchronize() {
        this.init();
        LOGGER.info("=== Synchronizing elements ===");
        if (this.plugin instanceof AbstractUpdaterPlugin) {
            this.updateAllElements();
        } else if (this.plugin instanceof AbstractRemakerPlugin) {
            this.remakeAllElements();
        } else {
            LOGGER.warn("Unable to synchronize with plugin " + this.plugin); 
        }
    }
    
    private void updateAllElements() {
        LOGGER.info(LOG_SEPARATOR);
        for (AbstractElement element : this.elementCache.getElements().values()) {
            this.counterForMatchedElements++;
            try {
                updateElement(element);
            } catch (java.lang.Exception e) {
                LOGGER.error("Synchronization of element " + element.getOsmId() + " has failed: ", e);
            }
            LOGGER.info(LOG_SEPARATOR);
        }    
    }

    private void updateElement(AbstractElement element) {
        if (element == null) {
            LOGGER.warn("Element is null, skipping it...");
            return;
        }
        LOGGER.info("Updating element #" + this.counterForMatchedElements + ": " + element);
        // Check if its best matching score is enough
        if (element.getMatchingScore() < this.plugin.getMinimalMatchingScore()) {
            LOGGER.info("Element cannot be updated because its matching score is "
                    + element.getMatchingScore() + " (min=" + this.plugin.getMinimalMatchingScore() + ")");
            return;
        }
        boolean needToSync = false;
        AbstractUpdaterPlugin updaterPlugin = (AbstractUpdaterPlugin) this.plugin;
        for (String updatableTagName : updaterPlugin.getUpdatableTagNames()) {
            LOGGER.info("* Updating data for the tag " + updatableTagName);
            // Check if tag is updatable
            if (updaterPlugin.isElementTagUpdatable(element, updatableTagName)) {
                boolean updated = updaterPlugin.updateElementTag(element, updatableTagName);
                if (updated) {
                    needToSync = true;
                }
            }
        }
        // Do the update sync only if needed
        if (needToSync) {
            boolean success = false;
            if ("api".equals(this.synchronizationMode)) {
                success = this.osmApiService.writeElement(element);
            } else if ("gen".equals(this.synchronizationMode)) {
                success = this.osmXmlFile.writeToFile("" + element.getOsmId(), element.getApiData());
            }
            if (success) {
                this.counterForAlteredElements++;
                element.setAltered(true);
                LOGGER.debug("Ok element has been updated");
            }
        } else {
            LOGGER.info("Element cannot be updated (maybe original value(s) exist(s))");
        }
    }
    
    private void remakeAllElements() {
        boolean success = false;
        OsmXmlRoot xmlForCreation = ((AbstractRemakerPlugin) this.plugin).getDataForCreation();
        OsmXmlRoot xmlForDeletion = ((AbstractRemakerPlugin) this.plugin).getDataForDeletion();
        if (xmlForCreation == null || xmlForDeletion == null) {
            LOGGER.warn("Unable to remake element since its remaking data is null");
            return;
        }
        if ("api".equals(this.synchronizationMode)) {
           // TODO direct api writing for remaking
        } else if ("gen".equals(this.synchronizationMode)) {
            success = this.osmXmlFile.writeToFile("genfile-creation", xmlForCreation) && this.osmXmlFile.writeToFile("genfile-deletion", xmlForDeletion);
        }
        if (success) {
            LOGGER.info("Ok all elements has been remaked");
        }
    }

    @Obsolete
    private void remakeElement(AbstractElement element) {
//        boolean success = false;
//        OsmApiRoot xml = ((AbstractRemakerPlugin) this.plugin).getNewElementsByElement(element.getOsmId());
//        if (xml == null) {
//            LOGGER.warn("Unable to sync element since its remaking data is null");
//            return;
//        }
//        if ("api".equals(this.synchronizationMode)) {
//           // TODO api writing for element remaking
//        } else if ("gen".equals(this.synchronizationMode)) {
//            success = this.osmXmlFile.writeToFile("genfile-" + element.getOsmId(), xml);
//        }
//        if (success) {
//            this.counterForAlteredElements++;
//            element.setAltered(true);
//            LOGGER.debug("Ok element has been remaked");
//        }
    }

}
