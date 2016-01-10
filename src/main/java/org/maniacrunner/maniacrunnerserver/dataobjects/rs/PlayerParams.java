package org.maniacrunner.maniacrunnerserver.dataobjects.rs;

import java.util.ArrayList;
import java.util.List;

public class PlayerParams{
	public String EffectVolumeValue;
	public String MusicVolumeValue;
	public String AddCoinBuffNumber;
	public String AddTimeBuffNumber;
	public String StopShootingBuffNumber;
	public String CoinNumber;
	public String CurrentAdventureId;
	public String Energy;
	public String UID;
	public List<AdventureParams> Adventures;
	
	public PlayerParams(){
		Adventures = new ArrayList<AdventureParams>();
	}
}
