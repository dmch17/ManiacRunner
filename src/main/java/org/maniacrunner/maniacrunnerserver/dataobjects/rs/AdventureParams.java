package org.maniacrunner.maniacrunnerserver.dataobjects.rs;

import java.util.ArrayList;
import java.util.List;

public class AdventureParams {
	public String AdventureId;
	public List<LevelParams> Levels;
	
	public AdventureParams() {}
	
	public AdventureParams(String adventureId){
		AdventureId = adventureId;
		Levels = new ArrayList<LevelParams>();
	}
}
