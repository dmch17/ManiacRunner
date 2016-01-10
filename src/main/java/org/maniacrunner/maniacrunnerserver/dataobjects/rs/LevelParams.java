package org.maniacrunner.maniacrunnerserver.dataobjects.rs;

public class LevelParams {
	public String Id;
	public String LevelId;
	public String BestResult;
	public String StarNumber;
	
	public LevelParams() {}
	
	public LevelParams(String id, String levelId, String bestResult, String starNumber){
		Id = id;
		LevelId = levelId;
		BestResult = bestResult;
		StarNumber = starNumber;
	}
}
