package org.openstreetmap.osmaxil.dao.xml.osm;

import javax.xml.bind.annotation.XmlAttribute;

public class OsmXmlTag {

	@XmlAttribute
	public String v;

	@XmlAttribute
	public String k;
	
	public OsmXmlTag() {
	}
	
	public OsmXmlTag(String v, String k) {
		this.v = v;
		this.k = k;
	}
}
