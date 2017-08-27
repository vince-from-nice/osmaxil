package org.openstreetmap.osmaxil.model;

import org.openstreetmap.osmaxil.model.misc.Coordinates;

public class CloudPointImport extends AbstractImport {
	
	private Coordinates coordinates;
	
	public CloudPointImport(String x, String y, String z) {
		this.coordinates = new Coordinates(x, y , z);
	}
	
	public CloudPointImport(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String getValueByTagName(String tagName) {
		return "Sorry, there is no tag for that type of import";
	}
	
	public String getX() {
		return this.coordinates.x;
	}
	
	public String gety() {
		return this.coordinates.y;
	}

	public String getZ() {
		return this.coordinates.z;
	}

	public Coordinates getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

}
