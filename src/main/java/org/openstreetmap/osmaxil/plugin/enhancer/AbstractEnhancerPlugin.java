package org.openstreetmap.osmaxil.plugin.enhancer;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractParser;

public class AbstractEnhancerPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends AbstractPlugin<ELEMENT, IMPORT> {

	@Override
	public void load() {
		// Load OSM elements matching the target area and target type
	}

	@Override
	public void process() {
		// 1. For each loaded element bind it with its matching imports

		// 2. For each matched element, compute its matching score
	}

	@Override
	public void synchronize() {
		// TODO Auto-generated method stub
	}

	@Override
	protected AbstractParser getParser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getChangesetComment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getChangesetSourceLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void displayProcessingStatistics() {
		// TODO Auto-generated method stub

	}

	@Override
	public void displaySynchronizingStatistics() {
		// TODO Auto-generated method stub

	}

}
