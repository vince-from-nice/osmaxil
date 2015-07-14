package org.openstreetmap.osmaxil.model.tree;

import java.util.List;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlTag;


public class TreeElement extends AbstractElement {

    public TreeElement(long osmId) {
        super(osmId);
        // TODO Auto-generated constructor stub
    }

    @Override
    public List<OsmXmlTag> getTags() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateChangeset(long changesetId) {
        // TODO Auto-generated method stub
    }

}
