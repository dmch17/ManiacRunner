package org.maniacrunner.maniacrunnerserver;

import org.maniacrunner.maniacrunnerserver.dataobjects.rq.AddCoinBuffNumberParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.AddTimeBuffNumberParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.CoinNumberParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.CurrentAdventureIdParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.EnergyParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.LevelDataParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.OpenAdventureParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.OpenLevelParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.PlayerAuthInfoParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.SoundParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.StopShootingBuffNumberParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rs.*;

public interface DataProvider {
	void setPlayerParams(PlayerParams playerParams);
	PlayerParams getPlayerParams(PlayerAuthInfoParamsRq playerInfoParamsRq, float energyPerMinute);
	GameParams getGameParams();
	EnergyParams updateEnergyParams(String playerId, float energyPerMinute);
	
	//new
	
	void reduceEnergy(String playerId, EnergyParamsRq energyParams);
	void reduceAddCoinBuffNumber(String playerId, AddCoinBuffNumberParamsRq addCoinBuffNumberParams);
	void reduceAddTimeNumber(String playerId, AddTimeBuffNumberParamsRq addTimeBuffNumberParams);
	void reduceStopShootingBuffNumber(String playerId, StopShootingBuffNumberParamsRq stopShootingBuffNumberParams);
	void setPlayerSound(String playerId, SoundParamsRq soundParams);
	void setPlayerCurrentAdventureId(String playerId, CurrentAdventureIdParamsRq currentAdventureIdParams);
	void changeCoinNumber(String playerId, CoinNumberParamsRq coinNumberParams);
	void changeLevelData(String playerId, LevelDataParamsRq levelDataParams);
	LevelParams openLevel(String playerId, OpenLevelParamsRq openLevelParams);
	void openAdventure(String playerId, OpenAdventureParamsRq openAdventureParams);
}
