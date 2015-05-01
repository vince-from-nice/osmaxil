package org.openstreetmap.osmaxil.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.annotation.Obsolete;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.plugin.AbstractElementMakerPlugin;
import org.openstreetmap.osmaxil.plugin.AbstractElementUpdaterPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElementSynchronizer extends AbstractService {

    private long counterForMatchedElements;

    private long counterForUpdatedElements;

    @Autowired
    private ElementCache elementCache;

    @PostConstruct
    public void init() {
        // Need to do here, when the synchronization phase is going to start to write elements.
        // If it would have been done on the Spring context initialization the first changeset could have become obsolete 
        // because changeset has an idle timeout of 1h and the previous phase (imports loading) could have taken more time.
        this.osmApiService.initForWriting(this.plugin.getChangesetSourceLabel(), this.plugin.getChangesetComment());
    }
    
    @PreDestroy
    public void close() {
        LOGGER.info("=== Closing element synchronizer ===");
        LOGGER.info("Total of matched elements: " + this.counterForMatchedElements);
        LOGGER.info("Total of updated elements: " + this.counterForUpdatedElements);
    }

    public void synchronizeElements() {
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
        if (this.plugin instanceof AbstractElementUpdaterPlugin) {
            
        } else if (this.plugin instanceof AbstractElementMakerPlugin) {
            
        } else {
            LOGGER.warn("Unable to synchronize with plugin " + this.plugin); 
        }
        // updateElementWithBestMatchingImport(element);
        updateElementWithBestAccumulatedImports(element);
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
        AbstractElementUpdaterPlugin updaterPlugin = (AbstractElementUpdaterPlugin) this.plugin;
        for (String updatableTagName : updaterPlugin.getUpdatableTagNames()) {
            LOGGER.info("* Updating data for the tag " + updatableTagName);
            // Check if its best matching score is enough
            if (element.getBestTotalScoreByTagName(updatableTagName) < updaterPlugin.getMinMatchingScoreForUpdate()) {
                LOGGER.info("Element cannot be updated because its best matching score is "
                        + element.getBestTotalScoreByTagName(updatableTagName) + " (min="
                        + updaterPlugin.getMinMatchingScoreForUpdate() + ")");
                return;
            }
            // Update tag value only if it is updatable (ie. no original value)
            if (updaterPlugin.isElementTagUpdatable(element, updatableTagName)) {
                updaterPlugin.updateElementTag(element, updatableTagName);
                needToWrite = true;
            }
        }
        if (needToWrite) {
            if (this.osmApiService.writeElement(element)) {
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
        AbstractElementUpdaterPlugin updaterPlugin = (AbstractElementUpdaterPlugin) this.plugin;
        // Check if its best matching score is enough
        if (element.getBestMatchingImport().getMatchingScore() < updaterPlugin.getMinMatchingScoreForUpdate()) {
            LOGGER.info("Element cannot be updated because its best matching score is "
                    + element.getBestMatchingImport().getMatchingScore() + " (min="
                    + updaterPlugin.getMinMatchingScoreForUpdate() + ")");
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
