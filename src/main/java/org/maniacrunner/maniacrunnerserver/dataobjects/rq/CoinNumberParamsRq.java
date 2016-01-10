package org.maniacrunner.maniacrunnerserver.dataobjects.rq;

public class CoinNumberParamsRq {
	
	public static String getHashString(int coinNumber, int UID){
        return String.format("%s%s", String.valueOf(coinNumber), String.valueOf(UID));
    }
	
    public String CoinNumber;
    public String Hash;
    
    public CoinNumberParamsRq() {}
}
