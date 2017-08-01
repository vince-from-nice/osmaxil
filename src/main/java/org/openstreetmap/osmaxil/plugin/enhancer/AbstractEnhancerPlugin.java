package org.openstreetmap.osmaxil.plugin.enhancer;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;

public abstract class AbstractEnhancerPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
                                            extends AbstractPlugin<ELEMENT, IMPORT> {

   abstract protected String getExistingElementQuery();

   // =========================================================================
   // Public methods
   // =========================================================================

   @Override
   public void process() {
      // Load all existing element IDs which are inside the filtering areas
      LOGGER.info("Looking in PostGIS for existing elements which are respecting the filtering areas");
      List<Long> elementIds = this.getExistingElements();

      // For each existing element, associate matched imports
      for (Long elementId : elementIds) {
         LOGGER.info("Binding element #" + elementId + ": ");
         if (elementId == null || elementId == 0) {
             LOGGER.warn("Element is null, skipping it...");
             break;
         }
         this.associateElementWithImports(elementId);
         LOGGER.info(LOG_SEPARATOR);
      }
   }

   @Override
   public void synchronize() {
      // TODO Auto-generated method stub
   }

   @Override
   public void displayProcessingStatistics() {
      // TODO Auto-generated method stub
   }

   @Override
   public void displaySynchronizingStatistics() {
      // TODO Auto-generated method stub
   }

   // =========================================================================
   // Private methods
   // =========================================================================

   private List<Long> getExistingElements() {
      List<Long> results = new ArrayList<>();
      String condition = "ST_Intersects(way, ST_GeomFromText(" + includingAreaString + "))";
      condition += "AND ST_Disjoint(way, ST_GeomFromText(" + includingAreaString + "))";
      String query = this.getExistingElementQuery();
      if (query.indexOf(" WHERE ") == -1) {
         query += " WHERE " + condition;
      } else {
         query += " and " + condition;
      }
      LOGGER.debug("Used query is: " + query);
      this.osmPostgis.findElementIdsByQuery(query);
      LOGGER.debug("Number of returned element IDs: " + query);
      return results;
   }
}
