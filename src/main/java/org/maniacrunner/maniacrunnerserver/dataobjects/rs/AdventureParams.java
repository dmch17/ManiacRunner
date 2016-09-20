package org.maniacrunner.maniacrunnerserver.dataobjects.rs;

import java.util.ArrayList;
import java.util.List;

public class AdventureParams {
	public String AdventureId;
	public String AdventureCost;
	public List<LevelParams> Levels;
	
	public AdventureParams(){
		Levels = new ArrayList<LevelParams>();
	}
	
/*	public AdventureParams(String id, String adventureId, String adventureCost){
		Id = id;
		AdventureId = adventureId;
		Levels = new ArrayList<LevelParams>();
	}*/
}
