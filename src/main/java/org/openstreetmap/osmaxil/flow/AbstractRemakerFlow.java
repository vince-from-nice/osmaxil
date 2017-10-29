package org.openstreetmap.osmaxil.flow;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.openstreetmap.osmaxil.Exception;
import org.openstreetmap.osmaxil.dao.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.misc.MatchingElementId;
import org.openstreetmap.osmaxil.plugin.selector.AbstractMatchingScoreSelector;
import org.openstreetmap.osmaxil.util.IdIncrementor;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractRemakerFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends _AbstractDrivenByImportFlow<ELEMENT, IMPORT> {

	// =========================================================================
	// Instance variables
	// =========================================================================

	protected Map<Long, ELEMENT> matchedElements = new Hashtable<Long, ELEMENT>();

	protected Map<Long, ELEMENT> remakableElements = new Hashtable<Long, ELEMENT>();

	@Autowired
	@Resource(name = "${selector}")
	protected AbstractMatchingScoreSelector<ELEMENT> selector;

	protected OsmXmlRoot dataForCreation;

	protected OsmXmlRoot dataForDeletion;

	private int counterForMatchedImports;

	private int counterForRemakedElements;

	IdIncrementor idGenerator = new IdIncrementor(1);

	// =========================================================================
	// Abstract methods
	// =========================================================================

	abstract protected boolean isElementRemakable(ELEMENT element);

	abstract protected ELEMENT instanciateElement(long osmId);

	abstract protected void processElement(ELEMENT element);

	abstract protected void buildDataForCreation();

	abstract protected void buildDataForDeletion();

	// =========================================================================
	// Public methods
	// =========================================================================

	@Override
	public void process() {
		// For each loaded import, bind it with its matching elements
		int importNbr = 0;
		for (IMPORT imp : this.loadedImports) {
			LOGGER.info("Binding import #" + importNbr + ": " + imp);
			importNbr++;
			if (imp == null) {
				LOGGER.warn("Import is null, skipping it...");
				break;
			}
			this.associateImportsWithElements(imp);
			LOGGER.info(LOG_SEPARATOR);
		}
		// For each matched element, compute its matching score and process it if it's remakable
		int elementNbr = 0;
		for (ELEMENT element : this.matchedElements.values()) {
			LOGGER.info("Computing matching score for element #" + elementNbr + ": " + element);
			if (element == null) {
				LOGGER.warn("Element is null, skipping it...");
				break;
			}
			this.computeMatchingScores(element);
			if (this.isElementRemakable(element)) {
				this.remakableElements.put(element.getOsmId(), element);
				this.processElement(element);
			}
			LOGGER.info(LOG_SEPARATOR);
		}
		// Build global remaking data
		this.buildDataForCreation();
		this.buildDataForDeletion();
	}

	@Override
	public void synchronize() {
		boolean success = false;
		if (this.dataForCreation == null || this.dataForDeletion == null) {
			LOGGER.warn("Unable to synchronize element because data is null");
			return;
		}
		if ("api".equals(this.synchronizationMode)) {
			// TODO direct api writing for remaking
		} else if ("gen".equals(this.synchronizationMode)) {
			success = this.osmXmlFile.writeToFile("genfile-creation", this.dataForCreation)
					&& this.osmXmlFile.writeToFile("genfile-deletion", this.dataForDeletion);
		}
		if (success) {
			LOGGER.info("Ok all elements has been synchronized");
			this.counterForRemakedElements++;
			// element.setAltered(true);
		}
	}

	@Override
	public void displayProcessingStatistics() {
		LOGGER_FOR_STATS.info("=== Processing statistics ===");
		LOGGER_FOR_STATS.info("Total of imports which match one or more matching elements: " + this.counterForMatchedImports);
		LOGGER_FOR_STATS.info("Total of elements which have one or more matching imports: " + this.matchedElements.size());
		LOGGER_FOR_STATS.info("Remaking data has been finalized as follow:");
		LOGGER_FOR_STATS.info("\tNodes: " + this.dataForCreation.nodes.size() + "");
		LOGGER_FOR_STATS.info("\tWays: " + this.dataForCreation.ways.size() + "");
		LOGGER_FOR_STATS.info("\tRelations: " + this.dataForCreation.relations.size());
	}

	@Override
	public void displaySynchronizingStatistics() {
		LOGGER_FOR_STATS.info("=== Synchronizing statistics ===");
		LOGGER_FOR_STATS.info("Total of remaked elements: " + this.counterForRemakedElements);
	}

	// =========================================================================
	// Private methods
	// =========================================================================

	// TODO make these private methods common with the AbstractUpdaterPlugin

	private void associateImportsWithElements(IMPORT imp) {
		// Find relevant element
		List<MatchingElementId> matchingElementIds = this.matcher.findMatchingElements(imp, this.parser.getSrid());
		if (matchingElementIds.size() > 0) {
			this.counterForMatchedImports++;
		}
		// For each matching elements
		for (MatchingElementId matchingElementId : matchingElementIds) {
			long osmId = matchingElementId.getOsmId();
			// Skip negative IDs (ie. multipolygon relations whose outer member has not been found)
			if (osmId < 0) {
				break;
			}
			// Get related element from the cache or create it
			ELEMENT element = null;
			try {
				element = this.getOrCreateElement(matchingElementId);
			} catch (Exception e) {
				LOGGER.error("Skipping element id=" + osmId + " (" + e.getMessage() + ")");
				break;
			}
			LOGGER.info(element);
			// And bind the import to it
			element.getMatchingImports().add(imp);
			imp.setMatchingElement(element);
			StringBuilder sb = new StringBuilder("Matching imports are now : [ ");
			for (AbstractImport i : element.getMatchingImports()) {
				sb.append(i.getId() + " ");
			}
			LOGGER.info(sb.append("]").toString());
		}
	}

	private ELEMENT getOrCreateElement(MatchingElementId relevantElementId) throws Exception {
		long osmId = relevantElementId.getOsmId();
		ELEMENT element = this.matchedElements.get(osmId);
		if (element == null) {
			// Fetch data from OSM API
			OsmXmlRoot apiData = this.osmStandardApi.readElement(osmId, element.getType());
			if (apiData == null) {
				throw new Exception("Unable to fetch data from OSM API for element#" + osmId);
			}
			// Instanciate a new element
			element = this.instanciateElement(osmId);
			element.setRelationId(relevantElementId.getRelationId());
			element.setApiData(apiData);
			this.matchedElements.put(osmId, element);
		}
		return element;
	}

	private void computeMatchingScores(ELEMENT element) {
		try {
			// Compute a matching score for each import matching the element
			for (AbstractImport imp : element.getMatchingImports()) {
				imp.setMatchingScore(this.matcher.computeMatchingImportScore((IMPORT) imp));
			}
			// Compute a global matching score for the element
			element.setMatchingScore(selector.computeElementMatchingScore(element));
		} catch (java.lang.Exception e) {
			LOGGER.error("Process of element " + element.getOsmId() + " has failed: ", e);
		}
		LOGGER.info(LOG_SEPARATOR);
	}
}
