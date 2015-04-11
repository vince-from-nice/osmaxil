package org.openstreetmap.osmaxil.service;

import javax.annotation.PreDestroy;

import org.apache.http.annotation.Obsolete;
import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.data.AbstractElement;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ElementSynchronizer {

    private long counterForMatchedElements;

    private long counterForUpdatedElements;

    @Autowired
    private ElementCache elementCache;

    @Autowired
    @Qualifier(value = "OpenDataParisBuildingPlugin")
    private AbstractPlugin plugin;

    @Autowired
    private OsmApiService osmApiService;

    static private final Logger LOGGER = Logger.getLogger(Application.class);

    static private final String LOG_SEPARATOR = "==========================================================";

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
        // synchronizeWithBestMatchingImport(element);
        synchronizeWithBestAccumulatedImports(element);
    }

    /**
     * Synchronize element to OSM API with tag values which are coming from the import list which haves the best total
     * matching score. This method is based on the new matching method where matching imports have been regrouped by
     * their tag values.
     * 
     * @param element
     */
    private void synchronizeWithBestAccumulatedImports(AbstractElement element) {
        boolean needToWrite = false;
        for (String updatableTagName : this.plugin.getUpdatableTagNames()) {
            LOGGER.info("* Updating data for the tag " + updatableTagName);
            // Check if its best matching score is enough
            if (element.getBestTotalScoreByTagName(updatableTagName) < this.plugin.getMinMatchingScoreForUpdate()) {
                LOGGER.info("Element cannot be updated because its best matching score is "
                        + element.getBestTotalScoreByTagName(updatableTagName) + " (min="
                        + this.plugin.getMinMatchingScoreForUpdate() + ")");
                return;
            }
            // Update tag value only if it is updatable (ie. no original value)
            if (this.plugin.isElementTagUpdatable(element, updatableTagName)) {
                this.plugin.updateElementTag(element, updatableTagName);
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
     * Synchronize element to OSM API with tag values which are coming from the best matching imports. This method is
     * now obsolete since the new matching method.
     * 
     * @param element
     */
    @Obsolete
    private void synchronizeWithBestMatchingImport(AbstractElement element) {
        // Check if its best matching score is enough
        if (element.getBestMatchingImport().getMatchingScore() < this.plugin.getMinMatchingScoreForUpdate()) {
            LOGGER.info("Element cannot be updated because its best matching score is "
                    + element.getBestMatchingImport().getMatchingScore() + " (min="
                    + this.plugin.getMinMatchingScoreForUpdate() + ")");
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
