package org.openstreetmap.osmaxil.dao.xml.osm;

import javax.xml.bind.annotation.XmlAttribute;

public class OsmXmlTag {

	@XmlAttribute
	public String v;

	@XmlAttribute
	public String k;
	
	public OsmXmlTag() {
	}
	
	public OsmXmlTag(String key, String value) {
		this.k = key;
		this.v = value;
	}
}
