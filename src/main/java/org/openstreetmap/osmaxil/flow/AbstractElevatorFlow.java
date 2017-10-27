package org.openstreetmap.osmaxil.flow;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.openstreetmap.osmaxil.dao.ElevationDataSource;
import org.openstreetmap.osmaxil.dao.ElevationDatabase;
import org.openstreetmap.osmaxil.dao.ElevationRasterFile;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.plugin.loader.AbstractElevationDbLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractElevatorFlow<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends _AbstractDrivenByElementFlow<ELEMENT, IMPORT> {

	private static final String UPDATABLE_TAG_NAMES[] = new String[] { ElementTag.HEIGHT };

	@Value("${elevator.shrinkRadius}")
	public int shrinkRadius;

	@Value("${elevator.minMatchingPoints}")
	public int minMatchingPoints;

	@Value("${elevator.computingDistance}")
	public int computingDistance;

	@Value("${elevator.toleranceDelta}")
	public float toleranceDelta;

	///////////////////////////////
	// Digital Terrain Model (DTM)
	///////////////////////////////
	
	protected ElevationDataSource dtm;

	@Value("${elevator.dtm.valueScale}")
	public float dtmValueScale;
	
	@Value("${elevator.dtm.minValue}")
	public int dtmMinValue;
	
	@Value("${elevator.dtm.maxValue}")
	public int dtmMaxValue;
	
	@Value("${elevator.dtm.type}")
	public String dtmType;
	
	@Value("${elevator.dtm.source}")
	public String dtmSource;

	@Value("${elevator.dtm.srid}")
	public int dtmSrid;

	@Autowired
	@Resource(name = "${elevator.dtm.loader}")
	protected AbstractElevationDbLoader dtmLoader;

	///////////////////////////////
	// Digital Surface Model (DSM)
	///////////////////////////////
	
	protected ElevationDataSource dsm;

	@Value("${elevator.dsm.valueScale}")
	public float dsmValueScale;
	
	@Value("${elevator.dsm.minValue}")
	public int dsmMinValue;
	
	@Value("${elevator.dtm.maxValue}")
	public int dsmMaxValue;
	
	@Value("${elevator.dsm.type}")
	public String dsmType;

	@Value("${elevator.dsm.source}")
	public String dsmSource;

	@Value("${elevator.dsm.srid}")
	public int dsmSrid;

	@Autowired
	@Resource(name = "${elevator.dsm.loader}")
	protected AbstractElevationDbLoader dsmLoader;

	@PostConstruct
	// TODO redo that part with Spring IoC feature (warning: I had some issues with prototype scoped beans) 
	void init() {
		// Init of the DTM
		if (dtmType.equals(ElevationDataSource.Type.DB.name())) this.dtm = new ElevationDatabase(this.dtmSource, this.dtmSrid, (JdbcTemplate) this.appContext.getBean("elevationPostgisJdbcTemplate"));
		if (dtmType.equals(ElevationDataSource.Type.FILE.name())) this.dtm = new ElevationRasterFile(this.dtmSource, this.dtmSrid);
		// Init of the DSM
		if (dsmType.equals(ElevationDataSource.Type.DB.name())) this.dsm = new ElevationDatabase(this.dsmSource, this.dsmSrid, (JdbcTemplate) this.appContext.getBean("elevationPostgisJdbcTemplate"));
		if (dsmType.equals(ElevationDataSource.Type.FILE.name())) this.dsm = new ElevationRasterFile(this.dtmSource, this.dtmSrid);
	}

	@Override
	public void load() {
		try {
			if (dtmType.equals("db")) this.dtmLoader.load((ElevationDatabase) this.dtm, this.dtmSource);
			if (dsmType.equals("db")) this.dsmLoader.load((ElevationDatabase) this.dsm, this.dsmSource);
		} catch (Exception e) {
			LOGGER.error(e);
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
		LOGGER_FOR_STATS.info(" - Shrink radius is: " + this.shrinkRadius);
		LOGGER_FOR_STATS.info(" - Minimum matching point is: " + this.minMatchingPoints);
		LOGGER_FOR_STATS.info(" - Computing distance is: " + this.computingDistance);
		LOGGER_FOR_STATS.info(" - Tolerance delta is: " + this.toleranceDelta);
	}
	
	protected boolean checkElevationValue(float value, ElevationDataSource.Use elevationType) {
		if (ElevationDataSource.Use.DTM == elevationType) {
			if (value > this.dtmMinValue && value < this.dtmMaxValue) return true;
		} else if (ElevationDataSource.Use.DSM == elevationType) {
			if (value > this.dsmMinValue && value < this.dsmMaxValue) return true;
		}
		return false;
	}

}
