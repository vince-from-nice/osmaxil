package org.openstreetmap.osmaxil.plugin.common.parser;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.openstreetmap.osmaxil.model.BuildingImport;
import org.springframework.stereotype.Repository;

@Repository
public class NiceBuildingParser extends AbstractParser<BuildingImport> {

    @PostConstruct
    public void init() throws IOException {
    	
    }
    
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BuildingImport next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSrid() {
		// TODO Auto-generated method stub
		return 0;
	}

}
