package org.openstreetmap.osmaxil.plugin.enhancer;

import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.AbstractImport;
import org.openstreetmap.osmaxil.plugin.AbstractPlugin;
import org.openstreetmap.osmaxil.plugin.common.matcher.AbstractMatcher;
import org.openstreetmap.osmaxil.plugin.common.parser.AbstractParser;
import org.openstreetmap.osmaxil.plugin.common.scorer.AbstractMatchingScorer;

public abstract class AbstractEnhancerPlugin<ELEMENT extends AbstractElement, IMPORT extends AbstractImport>
		extends AbstractPlugin<ELEMENT, IMPORT> {

}
