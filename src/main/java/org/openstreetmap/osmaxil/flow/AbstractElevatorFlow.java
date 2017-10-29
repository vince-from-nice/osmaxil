package org.openstreetmap.osmaxil.flow;

import static org.openstreetmap.osmaxil.dao.ElevationDataSource.Type.DB;
import static org.openstreetmap.osmaxil.dao.ElevationDataSource.Type.FILE;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.Exception;
import org.openstreetmap.osmaxil.dao.ElevationDataSource;
import org.openstreetmap.osmaxil.dao.ElevationDatabase;
import org.openstreetmap.osmaxil.dao.ElevationRasterFile;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.model.ElementTag;
import org.openstreetmap.osmaxil.plugin.loader.AbstractElevationDbLoader;
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

	protected int counterForOutsideDtmValues;

	protected int counterForOutsideDsmValues;

	///////////////////////////////
	// Digital Terrain Model (DTM)
	///////////////////////////////

	protected ElevationDataSource dtm;

	@Value("${elevator.dtm.type}")
	public String dtmType;

	@Value("${elevator.dtm.source}")
	public String dtmSource;

	@Value("${elevator.dtm.valueScale}")
	public float dtmValueScale;

	@Value("${elevator.dtm.minValue}")
	public int dtmMinValue;

	@Value("${elevator.dtm.maxValue}")
	public int dtmMaxValue;

	@Value("${elevator.dtm.srid}")
	public int dtmSrid;

	// @Autowired
	// @Resource(name = "${elevator.dtm.loader}")
	protected AbstractElevationDbLoader dtmLoader;

	@Value("${elevator.dtm.loader:}")
	public String dtmLoaderType;

	///////////////////////////////
	// Digital Surface Model (DSM)
	///////////////////////////////

	protected ElevationDataSource dsm;

	@Value("${elevator.dsm.type}")
	public String dsmType;

	@Value("${elevator.dsm.source}")
	public String dsmSource;

	@Value("${elevator.dsm.valueScale}")
	public float dsmValueScale;

	@Value("${elevator.dsm.minValue}")
	public int dsmMinValue;

	@Value("${elevator.dtm.maxValue}")
	public int dsmMaxValue;

	@Value("${elevator.dsm.srid}")
	public int dsmSrid;

	// @Autowired
	// @Resource(name = "${elevator.dsm.loader}")
	protected AbstractElevationDbLoader dsmLoader;

	@Value("${elevator.dsm.loader:}")
	public String dsmLoaderType;

	@PostConstruct
	// TODO redo that part with Spring IoC feature (warning: I had some issues with prototype scoped beans)
	void init() {
		// Init of the DTM
		if (dtmType.equals(DB.name()))
			this.dtm = new ElevationDatabase(this.dtmSource, this.dtmSrid, (JdbcTemplate) this.appContext.getBean("elevationPostgisJdbcTemplate"));
		if (dtmType.equals(FILE.name()))
			this.dtm = new ElevationRasterFile(this.dtmSource, this.dtmSrid);
		// Init of the DSM
		if (dsmType.equals(DB.name()))
			this.dsm = new ElevationDatabase(this.dsmSource, this.dsmSrid, (JdbcTemplate) this.appContext.getBean("elevationPostgisJdbcTemplate"));
		if (dsmType.equals(FILE.name()))
			this.dsm = new ElevationRasterFile(this.dtmSource, this.dtmSrid);
	}

	@Override
	public void load() throws Exception {
		String pkg = AbstractElevationDbLoader.class.getPackage().getName() + ".";
		try {
			if (dtmType.equals(DB.name())) {
				this.dtmLoader = (AbstractElevationDbLoader) Class.forName(pkg + dtmLoaderType).newInstance();
				this.dtmLoader.load((ElevationDatabase) this.dtm, this.dtmSource);
			}
			if (dsmType.equals(DB.name())) {
				this.dsmLoader = (AbstractElevationDbLoader) Class.forName(pkg + dsmLoaderType).newInstance();
				this.dsmLoader.load((ElevationDatabase) this.dsm, this.dsmSource);
			}
		} catch (java.lang.Exception e) {
			throw new Exception("Unable to load elevation database: " + e.getMessage());
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
		LOGGER_FOR_STATS.info("Specific stats of the plugin:");
		LOGGER_FOR_STATS.info(" - Out of range DTM values: " + this.counterForOutsideDtmValues);
		LOGGER_FOR_STATS.info(" - Out of range DSM values: " + this.counterForOutsideDsmValues);
		LOGGER_FOR_STATS.info("Specific settings of the plugin:");
		LOGGER_FOR_STATS.info(" - Shrink radius is: " + this.shrinkRadius);
		LOGGER_FOR_STATS.info(" - Minimum matching point is: " + this.minMatchingPoints);
		LOGGER_FOR_STATS.info(" - Computing distance is: " + this.computingDistance);
		LOGGER_FOR_STATS.info(" - Tolerance delta is: " + this.toleranceDelta);
	}

	protected boolean checkElevationValue(float value, ElevationDataSource.Use elevationType) {
		if (ElevationDataSource.Use.DTM == elevationType) {
			if (value < this.dtmMinValue || value > this.dtmMaxValue) {
				this.counterForOutsideDtmValues++;
				return false;
			}
		} else if (ElevationDataSource.Use.DSM == elevationType) {
			if (value < this.dsmMinValue || value > this.dsmMaxValue) {
				this.counterForOutsideDsmValues++;
				return false;
			}
		}
		return true;
	}

}
