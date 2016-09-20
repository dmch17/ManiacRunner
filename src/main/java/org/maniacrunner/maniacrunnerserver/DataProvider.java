package org.maniacrunner.maniacrunnerserver;

import org.maniacrunner.maniacrunnerserver.dataobjects.rq.*;
import org.maniacrunner.maniacrunnerserver.dataobjects.rs.*;

public interface DataProvider {
	//void setPlayerParams(PlayerParams playerParams);

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
	
	//actual
	GameParams getGameParams();
	PlayerParams getPlayerParams(PlayerAuthInfoParamsRq playerInfoParamsRq, float energyPerMinute);
	LevelStartedParamsRs setLevelStarted(String playerId, LevelStartedParamsRq levelStartedParams);
	LevelFinalizedParamsRs setLevelFinalized(String playerId, LevelFinalizedParamsRq levelFinalizedParams);
	LevelResetParamsRs setLevelReset(String playerId, LevelResetParamsRq levelResetParams);
}
