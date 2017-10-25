package org.openstreetmap.osmaxil.dao;

import java.util.ArrayList;
import java.util.List;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.openstreetmap.osmaxil.model.misc.Coordinates;

//@Service("ElevationRasterFile") @Lazy @Scope("prototype")
public class ElevationRasterFile implements ElevationDataSource {

	private String filePath;

	private int srid;

	private Dataset dataset;

	private double xUpperLeft, yUpperLeft;

	private double xPixelSize, yPixelSize;

	private List<Band> bands;
	
	public ElevationRasterFile(String source, int srid) {
		this.init(source, srid);
	}

	@Override
	public void init(String source, int srid) {
		this.filePath = source;
		this.srid = srid;
		gdal.AllRegister();
		LOGGER.info("Opening " + this.filePath);
		this.dataset = gdal.Open(this.filePath, gdalconstConstants.GA_ReadOnly);
		int numBands = dataset.getRasterCount();
		bands = new ArrayList<>();
		LOGGER.info("Number of bands is: " + numBands);
		for (int i = 1; i <= numBands; i++) {
			this.bands.add(dataset.GetRasterBand(i));
		}
		// Store the coordinates of the lower left point
		double[] geotransform = dataset.GetGeoTransform();
		this.xUpperLeft = geotransform[0];
		this.yUpperLeft = geotransform[3];
		LOGGER.info("Upper left coordinates are: " + this.xUpperLeft + " " + this.yUpperLeft);
		// Store the pixel sizes
		this.xPixelSize = geotransform[1];
		this.yPixelSize = Math.abs(geotransform[5]);
		LOGGER.info("Pixel sizes are: " + this.xPixelSize + " " + this.yPixelSize);
	}

	@Override
	public double findElevationByCoordinates(double x, double y, int srid) {
		int xFile = (int) Math.round((x - xUpperLeft) / this.xPixelSize);
		int yFile = (int) Math.round((yUpperLeft - y) / this.yPixelSize);
		double[] result = new double[1];
		bands.get(0).ReadRaster(xFile, yFile, 1, 1, result);
		return result[0];
	}

	@Override
	public List<Coordinates> findAllElevationsByGeometry(String includingGeomAsWKT, String excludingGeomAsWKT,
			int shrinkRadius, int geomSrid) {
		List<Coordinates> result = new ArrayList<>();
		// TODO the task is not trivial:
		// https://gis.stackexchange.com/questions/186483/how-to-get-the-pixels-from-a-geotiff-file-in-gdal-python-for-a-given-polygon
		return result;
	}

	@Override
	public int getSrid() {
		return srid;
	}

}
