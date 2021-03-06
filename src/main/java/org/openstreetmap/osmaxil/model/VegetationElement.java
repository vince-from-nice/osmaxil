package org.openstreetmap.osmaxil.model;

import java.util.List;

import org.openstreetmap.osmaxil.dao.xml.osm.OsmXmlTag;

public class VegetationElement extends AbstractElement {

	public VegetationElement(long osmId) {
		super(osmId);
	}

	@Override
	public ElementType getType() {
		return ElementType.Node;
	}

	@Override
	public List<OsmXmlTag> getTags() {
		return this.getApiData().nodes.get(0).tags;
	}

	@Override
	public void setTags(List<OsmXmlTag> tags) {
		this.getApiData().nodes.get(0).tags = tags;
	}

	@Override
	public void updateChangeset(long changesetId) {
		this.getApiData().nodes.get(0).changeset = changesetId;
	}

	public Double getLatitude() {
		return Double.parseDouble(this.getApiData().nodes.get(0).lat);
	}

	public Double getLongitude() {
		return Double.parseDouble(this.getApiData().nodes.get(0).lon);
	}

	public void setLatitude(Double lat) {
		this.getApiData().nodes.get(0).lat = lat.toString();
	}

	public void setLongitude(Double lon) {
		this.getApiData().nodes.get(0).lon = lon.toString();
	}

	@Override
	public String toString() {
		return "OSM tree has id=[" + this.getOsmId() + "], coords=[" + this.getLongitude() + " " + this.getLatitude() + "] genus=["
				+ this.getTagValue(ElementTag.GENUS) + "], species=[" + this.getTagValue(ElementTag.SPECIES) + "], name=[" + this.getName() + "]";
	}

}
