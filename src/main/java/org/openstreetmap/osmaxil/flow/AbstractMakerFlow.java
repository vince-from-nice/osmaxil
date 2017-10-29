package org.openstreetmap.osmaxil.flow;

import org.openstreetmap.osmaxil.dao.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.util.IdIncrementor;

public abstract class AbstractMakerFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends _AbstractDrivenByImportFlow<ELEMENT, IMPORT> {

	// =========================================================================
	// Instance variables
	// =========================================================================

	protected OsmXmlRoot dataForCreation;

	protected OsmXmlRoot dataForModification;

	protected OsmXmlRoot dataForDeletion;

	protected OsmXmlRoot dataForNonMakableElements;

	IdIncrementor idGenerator = new IdIncrementor(1);

	private int counterForMakableImports;

	private int counterForMakedImports;

	// =========================================================================
	// Abstract methods
	// =========================================================================

	abstract protected boolean isImportMakable(IMPORT imp);

	abstract protected void processImport(IMPORT imp);

	abstract protected void buildDataForCreation();

	abstract protected void buildDataForModification();

	abstract protected void buildDataForDeletion();

	abstract protected void buildDataForNonMakableElements();

	// =========================================================================
	// Public methods
	// =========================================================================

	@Override
	public void process() {
		int importNbr = 1;
		// For each import compute its matching score and process it if it's makable
		for (IMPORT imp : this.loadedImports) {
			LOGGER.info("Processing import #" + importNbr++ + ": " + imp);
			if (imp == null) {
				LOGGER.warn("Import is null, skipping it...");
				break;
			}
			imp.setMatchingScore(this.matcher.computeMatchingImportScore(imp));
			if (this.isImportMakable(imp)) {
				this.processImport(imp);
				this.counterForMakableImports++;
			}
			LOGGER.info(LOG_SEPARATOR);
		}
		this.buildDataForCreation();
		this.buildDataForModification();
		this.buildDataForDeletion();
		this.buildDataForNonMakableElements();
	}

	@Override
	public void synchronize() {
		boolean success = true;
		if (this.dataForCreation == null && this.dataForModification == null && this.dataForDeletion == null) {
			LOGGER.warn("Unable to synchronize because data is null");
			return;
		}
		if ("api".equals(this.synchronizationMode)) {
			// TODO direct api writing for making
		} else if ("gen".equals(this.synchronizationMode)) {
			if (this.dataForCreation != null) {
				success = success && this.osmXmlFile.writeToFile("genfile-for-creation", this.dataForCreation);
			}
			if (this.dataForModification != null) {
				success = success && this.osmXmlFile.writeToFile("genfile-for-modification", this.dataForModification);
			}
			if (this.dataForDeletion != null) {
				success = success && this.osmXmlFile.writeToFile("genfile-for-deletion", this.dataForDeletion);
			}
			if (this.dataForNonMakableElements != null) {
				success = success && this.osmXmlFile.writeToFile("genfile-for-non-makable-elements", this.dataForNonMakableElements);
			}
		}
		if (success) {
			LOGGER.info("Ok all imports has been synchronized");
			this.counterForMakedImports++;
		}
	}

	@Override
	public void displayProcessingStatistics() {
		LOGGER_FOR_STATS.info("=== Processing statistics ===");
		LOGGER_FOR_STATS.info("Total of makable imports: " + this.counterForMakableImports);
		LOGGER_FOR_STATS.info("Total of non makable imports: " + (this.counterForLoadedImports - this.counterForMakableImports));
	}

	@Override
	public void displaySynchronizingStatistics() {
		LOGGER_FOR_STATS.info("=== Synchronizing statistics ===");
		LOGGER_FOR_STATS.info("Total of maked imports: " + this.counterForMakedImports);
	}
}
