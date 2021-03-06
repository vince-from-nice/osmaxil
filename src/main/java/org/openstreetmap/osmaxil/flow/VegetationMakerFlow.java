package org.openstreetmap.osmaxil.flow;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmaxil.dao.xml.osm.OsmXmlNode;
import org.openstreetmap.osmaxil.dao.xml.osm.OsmXmlRoot;
import org.openstreetmap.osmaxil.dao.xml.osm.OsmXmlTag;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.model.VegetationElement;
import org.openstreetmap.osmaxil.model.VegetationImport;
import org.openstreetmap.osmaxil.model.misc.MatchingElementId;
import org.openstreetmap.osmaxil.plugin.matcher.VegetationImportMatcher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("VegetationMaker")
@Lazy
public class VegetationMakerFlow extends AbstractMakerFlow<VegetationElement, VegetationImport> {

	private List<OsmXmlRoot> newTreesToCreate = new ArrayList<>();

	private Map<Long, VegetationElement> existingTreesById = new HashMap<Long, VegetationElement>();

	private Map<Long, Float> bestScoreByExistingTreeId = new HashMap<Long, Float>();

	private Map<Long, VegetationImport> bestImportedTreeByExistingTreeId = new HashMap<Long, VegetationImport>();

	private Map<Long, List<MatchingElementId>> matchingTreeIdsByImportTreeId = new HashMap<>();

	private List<VegetationImport> nonMakableImportedTrees = new ArrayList<VegetationImport>();

	private int counterForMultiMatchingTrees;

	// =========================================================================
	// Overrided methods
	// =========================================================================

	@Override
	protected boolean isImportMakable(VegetationImport imp) {
		// Check if the imported tree is not inside an existing building
		if (this.isTreeInsideExistingBuilding(imp)) {
			LOGGER.warn("Tree #" + imp.getId() + " is inside an existing building => it's not makable");
			this.nonMakableImportedTrees.add(imp);
			return false;
		}
		// TODO Check if the imported tree is not on the sea 
		return true;
	}

	@Override
	protected void processImport(VegetationImport importedTree) {
		List<MatchingElementId> matchingElementIds = this.getMatchingTreesByImportedTree(importedTree);
		// If there's no matching tree, create a new tree from the import
		if (matchingElementIds.isEmpty()) {
			LOGGER.info("Tree has no matching tree, create a new tree from the import...");
			this.newTreesToCreate.add(this.createNewTreeFromImport(importedTree));
		}
		// Else watch was already done before...
		else {
			MatchingElementId bestMatchingElementId = matchingElementIds.get(0);
			long bestMatchingOsmId = bestMatchingElementId.getOsmId();
			LOGGER.info("Tree matches existing tree #" + bestMatchingOsmId);
			// If the best existing tree was not yet used create it and keep it
			VegetationElement existingTree = this.existingTreesById.get(bestMatchingOsmId);
			if (existingTree == null) {
				existingTree = this.createNewTreeFromExistingTree(bestMatchingOsmId, importedTree);
				this.existingTreesById.put(existingTree.getOsmId(), existingTree);
				this.bestScoreByExistingTreeId.put(bestMatchingOsmId, bestMatchingElementId.getScore());
				this.bestImportedTreeByExistingTreeId.put(existingTree.getOsmId(), importedTree);
			}
			// Else the matching tree is already matching another imported tree, need to do a special process
			else {
				this.counterForMultiMatchingTrees++;
				LOGGER.info("Best existing tree#" + bestMatchingOsmId + " is matching with more than one imported tree");
				processMultiMatchingTree(importedTree, 0);
			}
		}
	}

	@Override
	protected void buildDataForCreation() {
		OsmXmlRoot root = new OsmXmlRoot();
		for (OsmXmlRoot tree : this.newTreesToCreate) {
			root.nodes.add(tree.nodes.get(0));
		}
		this.dataForCreation = root;
	}

	@Override
	protected void buildDataForModification() {
		OsmXmlRoot root = new OsmXmlRoot();
		for (VegetationElement tree : this.existingTreesById.values()) {
			root.nodes.add(tree.getApiData().nodes.get(0));
		}
		this.dataForModification = root;
	}

	@Override
	protected void buildDataForDeletion() {
	}

	@Override
	protected void buildDataForNonMakableElements() {
		OsmXmlRoot root = new OsmXmlRoot();
		for (VegetationImport importedTree : this.nonMakableImportedTrees) {
			OsmXmlRoot xml = this.createNewTreeFromImport(importedTree);
			root.nodes.add(xml.nodes.get(0));
		}
		this.dataForNonMakableElements = root;
	}

	@Override
	public void displayProcessingStatistics() {
		super.displayProcessingStatistics();
		LOGGER_FOR_STATS.info("Matching area radius: " + ((VegetationImportMatcher) this.matcher).matchingAreaRadius);
		LOGGER_FOR_STATS.info("Matching closest only: " + ((VegetationImportMatcher) this.matcher).matchClosestOnly);
		LOGGER_FOR_STATS.info("Total of created trees: " + this.newTreesToCreate.size());
		LOGGER_FOR_STATS.info("Total of updated trees: " + this.existingTreesById.size());
		LOGGER_FOR_STATS.info("Total of created or updated trees: " + (newTreesToCreate.size() + existingTreesById.size()));
		LOGGER_FOR_STATS.info("Total of multi matching trees: " + this.counterForMultiMatchingTrees);
	}

	// =========================================================================
	// Private methods
	// =========================================================================

	private boolean isTreeInsideExistingBuilding(VegetationImport imp) {
		String geom = "ST_GeomFromText('POINT(" + imp.getLongitude() + " " + imp.getLatitude() + ")', " + this.parser.getSrid() + ")";
		// Transform geometry if it's needed
		if (this.parser.getSrid() != this.osmPostgis.getSrid()) {
			geom = "ST_Transform(" + geom + ", " + this.osmPostgis.getSrid() + ")";
		}
		String query = "select osm_id, 1 from planet_osm_polygon where building <> '' and  ST_Contains(way, " + geom + ");";
		LOGGER.debug("Looking in PostGIS for buildings containing coords: " + query);
		Long[] ids = this.osmPostgis.findElementIdsByQuery(query);
		if (ids.length > 0) {
			return true;
		} else {
			return false;
		}
	}

	private OsmXmlRoot createNewTreeFromImport(VegetationImport tree) {
		OsmXmlRoot root = new OsmXmlRoot();
		OsmXmlNode node = new OsmXmlNode();
		root.nodes.add(node);
		node.id = -this.idGenerator.getId();
		node.version = 0;
		// Add the tag natural=*
		node.tags.add(new OsmXmlTag(ElementTag.NATURAL, "tree"));
		// Fill all other tag values
		VegetationElement element = new VegetationElement(node.id);
		element.setApiData(root);
		setCoordsAndFillTagValuesIfNotExists(element, tree);
		return root;
	}

	private VegetationElement createNewTreeFromExistingTree(long osmId, VegetationImport importedTree) {
		VegetationElement tree = new VegetationElement(osmId);
		// Fetch data from the API
		tree.setApiData(this.osmStandardApi.readElement(tree.getOsmId(), tree.getType()));
		// Flag it as an element to modify
		tree.getApiData().nodes.get(0).action = "modify";
		// Move existing tree to the same position than the imported tree and "merge" the other attributes
		setCoordsAndFillTagValuesIfNotExists(tree, importedTree);
		return tree;
	}
	
	private void setCoordsAndFillTagValuesIfNotExists(VegetationElement treeElement, VegetationImport treeImport) {
		// Set the coordinates
		treeElement.setLatitude(treeImport.getLatitude());
		treeElement.setLongitude(treeImport.getLongitude());
		// Remove the source tag !
		if (treeElement.getTagValue(ElementTag.SOURCE) != null) {
			treeElement.removeTag(ElementTag.SOURCE);
		}
		// Set the tag values only if they don't exist yet
		if (this.useReferenceCode) {
			treeElement.setTagValue(ElementTag.REF + this.refCodeSuffix, treeImport.getReference());
		}
		if (treeElement.getTagValue(ElementTag.HEIGHT) == null && treeImport.getHeight() != null && treeImport.getHeight() > 0) {
			treeElement.setTagValue(ElementTag.HEIGHT, treeImport.getHeight().toString());
		}
		if (isBlank(treeElement.getTagValue(ElementTag.SPECIES)) && isNotBlank(treeImport.getSpecies())) {
			treeElement.setTagValue(ElementTag.SPECIES, treeImport.getSpecies());
		}
		// Don't set tag for genus if there is a already tag for species 
		if (isBlank(treeElement.getTagValue(ElementTag.GENUS)) && isNotBlank(treeImport.getGenus())
				&& isBlank(treeElement.getTagValue(ElementTag.SPECIES))) {
			treeElement.setTagValue(ElementTag.GENUS, treeImport.getGenus());
		}
		if (treeElement.getTagValue(ElementTag.CIRCUMFERENCE) == null && treeImport.getCircumference() != null && treeImport.getCircumference() > 0) {
			treeElement.setTagValue(ElementTag.CIRCUMFERENCE, treeImport.getCircumference().toString());
		}
		if (treeElement.getTagValue(ElementTag.START_DATE) == null && treeImport.getPlantingYear() != null && treeImport.getPlantingYear() > 0) {
			treeElement.setTagValue(ElementTag.START_DATE, treeImport.getPlantingYear().toString());
		}
	}

	private void processMultiMatchingTree(VegetationImport importedTree, int matchingElementIndex) {
		List<MatchingElementId> matchingElementIds = this.getMatchingTreesByImportedTree(importedTree);
		MatchingElementId matchingElementId = matchingElementIds.get(matchingElementIndex);
		LOGGER.info("Process multi matching tree for imported tree #" + importedTree.getId() + " with index=" + (1 + matchingElementIndex) + "/"
				+ matchingElementIds.size());
		long matchingOsmId = matchingElementId.getOsmId();
		Float previousBestScore = this.bestScoreByExistingTreeId.get(matchingElementId.getOsmId());
		// If we have a new winner (ie. it's closer to the best existing tree than the previous closest imported tree)
		if (previousBestScore == null || matchingElementId.getScore() > previousBestScore) {
			// The new best existing tree must be updated instead of created
			String txt = "Imported tree is closer to the existing tree #" + matchingOsmId + " than the previous closest imported tree ";
			if (previousBestScore != null) {
				txt += "(" + 1 / matchingElementId.getScore() + " < " + 1 / this.bestScoreByExistingTreeId.get(matchingElementId.getOsmId()) + ")";
			} else {
				txt += "(no previous best score yet)";
			}
			txt += " => can update it instead of create a new tree";
			LOGGER.info(txt);
			VegetationImport previousBestImportedTree = this.bestImportedTreeByExistingTreeId.get(matchingOsmId);
			VegetationElement newExistingTree = this.createNewTreeFromExistingTree(matchingOsmId, importedTree);
			this.existingTreesById.put(newExistingTree.getOsmId(), newExistingTree);
			this.bestScoreByExistingTreeId.put(newExistingTree.getOsmId(), matchingElementId.getScore());
			this.bestImportedTreeByExistingTreeId.put(newExistingTree.getOsmId(), importedTree);
			// If there was a previous best existing tree, its related imported tree must be reprocessed
			if (previousBestImportedTree != null) {
				LOGGER.info("Need to reprocess the previous best closest imported tree #" + previousBestImportedTree.getId());
				this.processMultiMatchingTree(previousBestImportedTree, 0);
			}
		} else {
			String txt = "Imported tree is NOT closer to the existing tree #" + matchingOsmId + " than the previous closest imported tree ";
			if (previousBestScore != null) {
				txt += "(" + 1 / matchingElementId.getScore() + " > " + 1 / this.bestScoreByExistingTreeId.get(matchingElementId.getOsmId()) + ")";
			}
			LOGGER.info(txt);
			// If there is another matching element for that import, retry with it
			if (matchingElementIndex < matchingElementIds.size() - 1) {
				this.processMultiMatchingTree(importedTree, ++matchingElementIndex);
			}
			// Else the imported tree cannot update an existing tree, it must created
			else {
				LOGGER.info("Imported tree doesn't have another matching tree => cannot update any existing tree, the tree must created");
				this.newTreesToCreate.add(createNewTreeFromImport(importedTree));
			}
		}
	}

	private List<MatchingElementId> getMatchingTreesByImportedTree(VegetationImport importedTree) {
		List<MatchingElementId> matchingElementIds = this.matchingTreeIdsByImportTreeId.get(importedTree.getId());
		// If the matching tree has not yet been calculated for that imported tree do it
		if (matchingElementIds == null) {
			matchingElementIds = this.matcher.findMatchingElements(importedTree, this.parser.getSrid());
			this.matchingTreeIdsByImportTreeId.put(importedTree.getId(), matchingElementIds);
		}
		return matchingElementIds;
	}
}
