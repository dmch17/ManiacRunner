package org.maniacrunner.maniacrunnerserver;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.maniacrunner.maniacrunnerserver.dataobjects.rq.*;
import org.maniacrunner.maniacrunnerserver.dataobjects.rs.*;

@Path("/gameservice")
public class GameService{
	public static final float EnergyPerMinute = 1f; 
	private DataProvider dataProvider;
	
	public GameService() {
		dataProvider = new MySqlDataProvider();
	}
	
	@GET
	@Path("/getgameparams")
	@Produces(MediaType.APPLICATION_JSON)
	public GameParams getGameParams(){
		return dataProvider.getGameParams();
	}
	
	@GET
	@Path("/getplayerparams/{playerId}/energy")
	@Produces(MediaType.APPLICATION_JSON)
	public EnergyParams updatePlayerEnergyParams(@PathParam("playerId") String playerId){
		return dataProvider.updateEnergyParams(playerId, GameService.EnergyPerMinute);
	}
	
	@POST
	@Path("/getplayerparams")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public PlayerParams getPlayerParams(PlayerAuthInfoParamsRq playerInfoParams){
		return dataProvider.getPlayerParams(playerInfoParams, GameService.EnergyPerMinute);
	}
	
	@POST
	@Path("/setplayerparams/{playerId}/value")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setPlayerValueParams(PlayerValueParamsRq params, @PathParam("playerId")String playerId){
	}
	
	@POST
	@Path("/setplayerparams/{playerId}/game")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setPlayerGameParams(PlayerValueParamsRq params, @PathParam("playerId")String playerId){
	}
	
	@POST
	@Path("/setplayerparams/{playerId}/adventure/{adventureParams}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setPlayerAdventureParams(){
	}
	
	@POST
	@Path("/setplayerparams/{playerId}/level/{levelParams}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setPlayerLevelParams(){
	}
	
	//new
	
	@POST
	@Path("/deleteplayerparams/{playerId}/energy")
	@Consumes(MediaType.APPLICATION_JSON)
	public void deletePlayerEnergyParams(EnergyParamsRq energyParams, @PathParam("playerId") String playerId){
		dataProvider.reduceEnergy(playerId, energyParams);
	}
	
	@POST
	@Path("/deleteplayerparams/{playerId}/addcoinbuffnumber")
	@Consumes(MediaType.APPLICATION_JSON)
	public void deletePlayerAddCoinBuffNumberParams(AddCoinBuffNumberParamsRq addCoinBuffNumberParams, @PathParam("playerId") String playerId) {
		dataProvider.reduceAddCoinBuffNumber(playerId, addCoinBuffNumberParams);
	}
	
	@POST
	@Path("deleteplayerparams/{playerId}/addtimebuffnumber")
	@Consumes(MediaType.APPLICATION_JSON)
	public void deletePlayerAddTimeBuffNumberParams(AddTimeBuffNumberParamsRq addTimeBuffNumberParams, @PathParam("playerId") String playerId){
		dataProvider.reduceAddTimeNumber(playerId, addTimeBuffNumberParams);
	}
	
	@POST
	@Path("deleteplayerparams/{playerId}/stopshootingbuffnumber")
	@Consumes(MediaType.APPLICATION_JSON)
	public void deletePlayerStopShootingBuffNumberParams(StopShootingBuffNumberParamsRq stopShootingBuffNumberParams, @PathParam("playerId") String playerId){
		dataProvider.reduceStopShootingBuffNumber(playerId, stopShootingBuffNumberParams);
	}
	
	@POST
	@Path("setplayerparams/{playerId}/sound")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setPlayerSoundParams(SoundParamsRq soundParams, @PathParam("playerId") String playerId){
		dataProvider.setPlayerSound(playerId, soundParams);
	}
	
	@POST
	@Path("setplayerparams/{playerId}/currentadventureid")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setPlayerCurrentAdventureIdParams(CurrentAdventureIdParamsRq currentAdventureIdParams, @PathParam("playerId") String playerId){
		dataProvider.setPlayerCurrentAdventureId(playerId, currentAdventureIdParams);
	}
	
	@POST
	@Path("setplayerparams/{playerId}/coinnumber")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setPlayerCoinParams(CoinNumberParamsRq coinNumberParams, @PathParam("playerId") String playerId){
		dataProvider.changeCoinNumber(playerId, coinNumberParams);
	}
	
	@POST
	@Path("setplayerparams/{playerId}/leveldata")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setPlayerLevelDataParams(LevelDataParamsRq levelDataParams, @PathParam("playerId") String playerId){
		dataProvider.changeLevelData(playerId, levelDataParams);
	}
	
	@POST
	@Path("createplayerparams/{playerId}/leveldata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public LevelParams createPlayerOpenLevelParams(OpenLevelParamsRq openLevelParams, @PathParam("playerId") String playerId){
		return dataProvider.openLevel(playerId, openLevelParams);
	}
	
	@POST
	@Path("createplayerparams/{playerId}/adventure")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createPlayerOpenAdventureParams(OpenAdventureParamsRq openAdventureParams, @PathParam("playerId") String playerId){
		dataProvider.openAdventure(playerId, openAdventureParams);
	}	
}
