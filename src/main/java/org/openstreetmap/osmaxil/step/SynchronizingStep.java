package org.openstreetmap.osmaxil.step;

import javax.annotation.PreDestroy;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.dao.ElementStore;
import org.openstreetmap.osmaxil.dao.OsmXml;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.plugin.AbstracRemakerPlugin;
import org.openstreetmap.osmaxil.plugin.AbstractUpdaterPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SynchronizingStep extends AbstractStep {

    private long counterForMatchedElements;

    private long counterForUpdatedElements;
    
    private long counterForRemakedElements;

    @Autowired
    private ElementStore elementCache;
    
    @Value("${osmaxil.sync}")
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
    
    @PreDestroy
    public void close() {
        LOGGER.info("=== Closing element synchronizer ===");
        LOGGER.info("Total of matched elements: " + this.counterForMatchedElements);
        LOGGER.info("Total of updated elements: " + this.counterForUpdatedElements);
        LOGGER.info("Total of remaked elements: " + this.counterForRemakedElements);
    }

    public void synchronizeElements() {
        this.init();
        LOGGER.info("=== Updating elements ===");
        LOGGER.info(LOG_SEPARATOR);
        for (AbstractElement element : this.elementCache.getElements().values()) {
            this.counterForMatchedElements++;
            try {
                synchronizeElement(element);
            } catch (java.lang.Exception e) {
                LOGGER.error("Synchronization of element " + element.getOsmId() + " has failed: ", e);
            }
            LOGGER.info(LOG_SEPARATOR);
        }
    }

    private void synchronizeElement(AbstractElement element) {
        if (element == null) {
            LOGGER.warn("Element is null, skipping it...");
            return;
        }
        LOGGER.info("Synchronizing element #" + this.counterForMatchedElements + ": " + element);
        // The synchronization process depends on the nature of the plugin
        if (this.plugin instanceof AbstractUpdaterPlugin) {
            // updateElementWithBestMatchingImport(element);
            updateElementWithBestAccumulatedImports(element);            
        } else if (this.plugin instanceof AbstracRemakerPlugin) {
            remakeElement(element);
        } else {
            LOGGER.warn("Unable to synchronize with plugin " + this.plugin); 
        }

    }
    
    private void remakeElement(AbstractElement element) {
        boolean success = false;
        if ("api".equals(this.synchronizationMode)) {
           // TODO api write for element remaking
        } else if ("gen".equals(this.synchronizationMode)) {
            success = this.osmXmlFile.writeToFile("id" + element.getOsmId(), element.getRemakingData());
        }
        if (success) {
            this.counterForRemakedElements++;
            element.setUpdated(true);
            LOGGER.debug("Ok element has been remaked");
        }
    }

    /**
     * Update element into OSM database with tag values which are coming from the import list which haves the best total
     * matching score. This method is based on the new matching method where matching imports have been regrouped by
     * their tag values.
     * 
     * @param element
     */
    private void updateElementWithBestAccumulatedImports(AbstractElement element) {
        boolean needToWrite = false;
        AbstractUpdaterPlugin updaterPlugin = (AbstractUpdaterPlugin) this.plugin;
        for (String updatableTagName : updaterPlugin.getUpdatableTagNames()) {
            LOGGER.info("* Updating data for the tag " + updatableTagName);
            // Check if its best matching score is enough
            if (element.getBestTotalScoreByTagName(updatableTagName) < updaterPlugin.getMinMatchingScore()) {
                LOGGER.info("Element cannot be updated because its best matching score is "
                        + element.getBestTotalScoreByTagName(updatableTagName) + " (min="
                        + updaterPlugin.getMinMatchingScore() + ")");
                return;
            }
            // Update tag value only if it is updatable (ie. no original value)
            if (updaterPlugin.isElementTagUpdatable(element, updatableTagName)) {
                updaterPlugin.updateElementTag(element, updatableTagName);
                needToWrite = true;
            }
        }
        if (needToWrite) {
            boolean success = false;
            if ("api".equals(this.synchronizationMode)) {
                success = this.osmApiService.writeElement(element);
            } else if ("gen".equals(this.synchronizationMode)) {
                // TODO file generation for element update
            }
            if (success) {
                this.counterForUpdatedElements++;
                element.setUpdated(true);
                LOGGER.debug("Ok element has been updated");
            }
        } else {
            LOGGER.info("Element cannot be updated (maybe original value(s) exist(s))");
        }
    }

    /**
     * Update element to OSM database with tag values which are coming from the best matching imports. This method is
     * now obsolete since the new matching method.
     * 
     * @param element
     */
    @Obsolete
    private void updateElementWithBestMatchingImport(AbstractElement element) {
        AbstractUpdaterPlugin updaterPlugin = (AbstractUpdaterPlugin) this.plugin;
        // Check if its best matching score is enough
        if (element.getBestMatchingImport().getMatchingScore() < updaterPlugin.getMinMatchingScore()) {
            LOGGER.info("Element cannot be updated because its best matching score is "
                    + element.getBestMatchingImport().getMatchingScore() + " (min="
                    + updaterPlugin.getMinMatchingScore() + ")");
            return;
        }
        // Try to update the element data with the best matching element
        boolean needToUpdate = false;
        // needToUpdate = this.plugin.updateElementData(element.getBestMatchingImport(), element);
        // Update element only if needed
        if (needToUpdate) {
            if (this.osmApiService.writeElement(element)) {
                this.counterForUpdatedElements++;
                LOGGER.debug("Ok element has been updated with import #" + element.getBestMatchingImport().getId());
            }
        } else {
            LOGGER.info("Element cannot be modified because original values exist");
        }
    }

}
