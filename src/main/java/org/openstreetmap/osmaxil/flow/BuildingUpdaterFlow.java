package org.openstreetmap.osmaxil.flow;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.BuildingElement;
import org.openstreetmap.osmaxil.model.BuildingImport;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.plugin.matcher.BuildingImportMatcher;
import org.openstreetmap.osmaxil.plugin.selector.CumulativeOnSameValueMatchingScoreSelector;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("BuildingUpdaterFlow")
@Lazy
public class BuildingUpdaterFlow extends AbstractUpdaterFlow<BuildingElement, BuildingImport> {

	private int counterForFakeNames = 0;

	private static final String UPDATABLE_TAG_NAMES[] = new String[] { ElementTag.HEIGHT, ElementTag.NAME, ElementTag.URL };

	private static final String MATCHING_TAG_NAME = ElementTag.BUILDING_LEVELS;

	// =========================================================================
	// Overrided methods
	// =========================================================================

	@Override
	protected boolean isElementTagUpdatable(BuildingElement element, String tagName) {
		// Building tags are updatable only if it doesn't have an original value, except for the tag url which can have multiples values
		return (ElementTag.URL.equals(tagName) ? true : element.getOriginalValuesByTagNames().get(tagName) == null);
	}

	@Override
	protected boolean updateElementTag(BuildingElement element, String tagName) {
		String tagValue = element.getMatchingImports().get(0).getValueByTagName(tagName);
		if (tagValue == null) {
			LOGGER.warn("Cannot update tag because tag value is null for " + tagName);
			return false;
		}
		boolean updated = false;
		if (ElementTag.HEIGHT.equals(tagName)) {
			element.setHeight(Integer.parseInt(tagValue));
			LOGGER.info("===> Updating height to [" + tagValue + "]");
			updated = true;
		}
		if (ElementTag.HEIGHT.equals(tagName)) {
			// Adding +1 to levels because OSM use the US way to count building levels
			// TODO make it configurable
			element.setLevels(Integer.parseInt(tagValue) + 1);
			LOGGER.info("===> Updating levels to " + (tagValue + 1));
			updated = true;
		}
		if (ElementTag.URL.equals(tagName)) {
			element.setTagValue(ElementTag.URL, tagValue);
			LOGGER.info("===> Updating URL to [" + tagValue + "]");
			updated = true;
		}
		if (ElementTag.NAME.equals(tagName)) {
			// Ignore fake name based on the building address
			if (Character.isDigit(tagValue.charAt(0))) {
				LOGGER.info("Skipping name update because the value looks fake (" + tagValue + ")");
				this.counterForFakeNames++;
			} else {
				element.setTagValue(ElementTag.NAME, tagValue);
				LOGGER.info("===> Updating name to [" + tagValue + "]");
				updated = true;
			}
		}
		return updated;
	}

	// TEMP just for stats: elements are updatable if all of the updatable tags are updatable with it
	// @Override
	// public boolean isElementAlterable(BuildingElement element) {
	// for (int j = 0; j < this.getUpdatableTagNames().length; j++) {
	// if(!this.isElementTagUpdatable(element, this.getUpdatableTagNames()[j])) {
	// return false;
	// }
	// }
	// return true;
	// }

	@Override
	public void displaySynchronizingStatistics() {
		super.displaySynchronizingStatistics();
		LOGGER_FOR_STATS.info("Total of fake names: " + this.counterForFakeNames);
	}

	@Override
	protected String[] getUpdatableTagNames() {
		return UPDATABLE_TAG_NAMES;
	}

	@Override
	protected BuildingElement instanciateElement(long osmId) {
		return new BuildingElement(osmId);
	}

	// =========================================================================
	// Private methods
	// =========================================================================

	@PostConstruct
	public void init() {
		if (this.matcher instanceof BuildingImportMatcher) {
			((BuildingImportMatcher) this.matcher).setWithSurfaces(false);
		}
		if (this.selector instanceof CumulativeOnSameValueMatchingScoreSelector) {
			((CumulativeOnSameValueMatchingScoreSelector<BuildingElement>) this.selector).setMatchingTagName(MATCHING_TAG_NAME);
		}
	}

}
