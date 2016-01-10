package org.maniacrunner.maniacrunnerserver.dataobjects.rq;

public class LevelDataParamsRq {
    public static String getHashString(long id, String levelId, int UID){
        return String.format("%s%s%s", String.valueOf(id), levelId, String.valueOf(UID));
    }

    public String Id;
    public String LevelId;
    public String BestResult;
    public String StarNumber;
    public String Hash;
    
    public LevelDataParamsRq() {}
}
