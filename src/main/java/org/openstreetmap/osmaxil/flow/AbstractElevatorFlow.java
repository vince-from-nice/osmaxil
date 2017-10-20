package org.openstreetmap.osmaxil.flow;

import org.openstreetmap.osmaxil.dao.ElevationDataSource;
import org.openstreetmap.osmaxil.dao.ElevationDatabase;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractElevatorFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport> extends _AbstractDrivenByElementFlow<ELEMENT, IMPORT> {

	private static final String UPDATABLE_TAG_NAMES[] = new String[] { ElementTag.HEIGHT };

	@Value("${elevator.minMatchingPoints}")
	public int minMatchingPoints;
	
	@Value("${elevator.computingDistance}")
	public int computingDistance;
	
	@Value("${elevator.toleranceDelta}")
	public float toleranceDelta;
	
	@Autowired
	protected ElevationDataSource dtmDataSource;
	
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
	
	@Override
	protected String[] getUpdatableTagNames() {
		return UPDATABLE_TAG_NAMES;
	}
	
	@Override
	public void displayProcessingStatistics() {
		super.displayProcessingStatistics();
		LOGGER_FOR_STATS.info("Specific settings of the plugin:");
		// LOGGER_FOR_STATS.info(" - Shrink radius is: " + this.shrinkRadius);
		LOGGER_FOR_STATS.info(" - Minimum matching point is: " + minMatchingPoints);
		LOGGER_FOR_STATS.info(" - Tolerance delta is: " + toleranceDelta);
	}

}
