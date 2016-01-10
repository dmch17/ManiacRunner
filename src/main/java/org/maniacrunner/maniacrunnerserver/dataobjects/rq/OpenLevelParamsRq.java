package org.maniacrunner.maniacrunnerserver.dataobjects.rq;

public class OpenLevelParamsRq {
    public static String getHashString(String adventureId, String levelId, int UID){
        return String.format("%s%s%s",adventureId, levelId, String.valueOf(UID));
    }

    //Точно ли AdventureId нужен?
    public String AdventureId;
    public String LevelId;
    public String Hash;
		
	public OpenLevelParamsRq() {}
}
