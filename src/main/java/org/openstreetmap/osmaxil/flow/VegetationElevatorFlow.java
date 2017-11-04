package org.openstreetmap.osmaxil.flow;

import java.util.List;

import org.openstreetmap.osmaxil.model.VegetationElement;
import org.openstreetmap.osmaxil.model.VegetationImport;

public class VegetationElevatorFlow extends AbstractElevatorFlow<VegetationElement, VegetationImport> {

	@Override
	protected List<VegetationElement> getTargetedElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<VegetationImport> findMatchingImports(VegetationElement element, int srid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	float computeElementMatchingScore(VegetationElement element, float minMatchingScore) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected VegetationElement instanciateElement(long osmId) {
		// TODO Auto-generated method stub
		return null;
	}



}
