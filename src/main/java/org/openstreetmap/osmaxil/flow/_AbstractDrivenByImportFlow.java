package org.openstreetmap.osmaxil.flow;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.matcher.AbstractImportMatcher;
import org.openstreetmap.osmaxil.plugin.parser.AbstractImportParser;
import org.openstreetmap.osmaxil.plugin.scorer.AbstractElementScorer;

public abstract class _AbstractDrivenByImportFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends _AbstractImportFlow<ELEMENT, IMPORT> {
	
    abstract protected AbstractImportParser<IMPORT> getParser();
    
    abstract protected AbstractImportMatcher<IMPORT> getMatcher();

    abstract protected AbstractElementScorer<ELEMENT> getScorer();
    
    protected long counterForParsedImports;

    protected long counterForFilteredImports;

    protected long counterForLoadedImports;
    
	// =========================================================================
	// Public and protected methods
	// =========================================================================
    
    @Override
    public void load() {
        while (this.getParser().hasNext()) {
            try {
                IMPORT imp = this.getParser().next();
                if (imp == null) {
                    LOGGER.warn("Import is null, skipping it...");
                    break;
                }
                this.counterForParsedImports++;
                LOGGER.info("Loading import #" + this.counterForParsedImports + ": " + imp);
                // Check if the import coordinates are fine with the filtering areas
                if (!this.checkCoordinatesWithFilteringArea(imp.getLongitude(), imp.getLatitude())) {
                    this.counterForFilteredImports++;
                    LOGGER.warn("Import has coordinates which are not respecting the filtering areas, skipping it...");
                } else {
                    this.loadedImports.add(imp);
                    this.counterForLoadedImports++;
                }
            } catch (java.lang.Exception e) {
                LOGGER.error("Unable to load an import: ", e);
            } finally {
                LOGGER.info(LOG_SEPARATOR);
            }
        }
    }

    @Override
    public void displayLoadingStatistics() {
        LOGGER_FOR_STATS.info("=== Loading statistics ===");
        LOGGER_FOR_STATS.info("Total of parsed imports: " + this.counterForParsedImports);
        LOGGER_FOR_STATS.info("Total of filtered out imports: " + this.counterForFilteredImports);
        LOGGER_FOR_STATS.info("Total of loaded imports: " + this.counterForLoadedImports);
    }
}
