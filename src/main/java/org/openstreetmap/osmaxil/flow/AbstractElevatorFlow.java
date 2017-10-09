package org.openstreetmap.osmaxil.flow;

import org.openstreetmap.osmaxil.dao.ElevationDataSource;
import org.openstreetmap.osmaxil.dao.ElevationDatabase;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.plugin.scorer.BuildingElevationScorer;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractElevatorFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport> extends _AbstractDrivenByElementFlow<ELEMENT, IMPORT> {

	private static final String UPDATABLE_TAG_NAMES[] = new String[] { ElementTag.HEIGHT };

	@Autowired
	protected ElevationDataSource dsmDataSource;

    @Override
    public void prepare() {
    	if (!this.skipPreparation) {
    		return;
    	}
    	if (this.dsmDataSource instanceof ElevationDatabase) {
    		((ElevationDatabase) this.dsmDataSource).createPointCloudTableFromXYZFiles();	
    	}    	
	}

	@Override
	public void displayProcessingStatistics() {
		super.displayProcessingStatistics();
		LOGGER_FOR_STATS.info("Specific settings of the plugin:");
		//LOGGER_FOR_STATS.info(" - Shrink radius is: " + this.shrinkRadius);
		if (this.scorer instanceof BuildingElevationScorer) {
			BuildingElevationScorer bdcs = (BuildingElevationScorer) this.scorer;
			LOGGER_FOR_STATS.info(" - Minimum matching point is: " + bdcs.minMatchingPoints);
			LOGGER_FOR_STATS.info(" - Tolerance delta is: " + bdcs.toleranceDelta);
		}
	}

	@Override
	protected String[] getUpdatableTagNames() {
		return UPDATABLE_TAG_NAMES;
	}
	
	@Override
	protected boolean isElementTagUpdatable(ELEMENT element, String tagName) {
		// Building tags are updatable only if it doesn't have an original value
		String originalValue = element.getOriginalValuesByTagNames().get(tagName);
		if (originalValue != null) {
			LOGGER.info("The tag " + tagName + " cannot be updated because it has an original value: " + originalValue);
			return false;
		}
		return true;
	}

	@Override
	protected boolean updateElementTag(ELEMENT element, String tagName) {
		boolean updated = false;
		if (ElementTag.HEIGHT.equals(tagName)) {
			if (element.getComputedHeight() == null) {
				LOGGER.error("Cannot update tag because computed height is null");
				return false;
			}
			element.setHeight(element.getComputedHeight());
			LOGGER.info("===> Updating height to [" + element.getHeight() + "]");
			updated = true;
		}
		return updated;
	}

}
