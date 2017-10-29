package org.openstreetmap.osmaxil.model;

public class ElevationImport extends AbstractImport {

	// private Coordinates coordinates;

	public float x, y, z;

	public ElevationImport(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.setId(Math.round(z));
	}

	@Override
	public String getValueByTagName(String tagName) {
		return "Sorry, there is no tag for that type of import";
	}

	@Override
	public String toString() {
		return "Elevation import with x=" + x + " y=" + y + " z=" + z;
	}

}
