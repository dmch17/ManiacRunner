package org.maniacrunner.maniacrunnerserver;

import java.util.Random;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Math;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.maniacrunner.maniacrunnerserver.dataobjects.rq.AddCoinBuffNumberParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.AddTimeBuffNumberParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.CoinNumberParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.CurrentAdventureIdParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.EnergyParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.LevelDataParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.LevelFinalizedParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.LevelResetParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.LevelStartedParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.OpenAdventureParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.OpenLevelParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.PlayerAuthInfoParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.SoundParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rq.StopShootingBuffNumberParamsRq;
import org.maniacrunner.maniacrunnerserver.dataobjects.rs.*;

public class MySqlDataProvider implements DataProvider{
	
	private static Logger logger;
	private static final String APPLICATION_ID = "5134254";
	private static final String SECRET_APPLICATION_KEY = "PQTpQ03SFNc2gZCBTWy2";
	private static final String SMALL_COIN_VALUE_NAME = "SmallCoinValue";
	private static final String MEDIUM_COIN_VALUE_NAME = "MediumCoinValue";
	private static final String BIG_COIN_VALUE_NAME = "BigCoinValue";
	private static final String ADD_COIN_BUFF_VALUE_NAME = "AddCoinBuffValue";
	private static final String ADD_COIN_BUFF_TIME_VALUE_NAME = "AddCoinBuffTimeValue";
	private static final String ADD_TIME_BUFF_VALUE_NAME = "AddTimeBuffValue";
	private static final String STOP_SHOOTING_BUFF_VALUE_NAME = "StopShootingBuffValue";
	private static final String UPDATE_ENERGY_FREQUENCY_NAME = "UpdateEnergyFrequency";
	private static final String FIRST_LEVEL_ID = "JStory_1";
	private static final String EMPTY_CONST = "-1";
	private static final int MAX_UID = 10000;
	
	static {
		MySqlDataProvider.logger = Logger.getLogger(MySqlDataProvider.class.getName());
	}
	
	private DataSource source;

	public MySqlDataProvider(){
		try{
			InitialContext context = new InitialContext();
			source = (DataSource)context.lookup("jdbc/ManiacRunnerResource");	
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
	}
	
/*	public void setPlayerParams(PlayerParams playerParams) {
		try{
//			Connection conn = source.getConnection();
//			(conn.prepareStatement(MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerParams(""))).executeQuery();
//			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
	}*/
	
	public GameParams getGameParams() {
		GameParams gameParams = new GameParams();  
		try{
			Connection conn = source.getConnection();
			ResultSet result = (conn.prepareStatement(MySqlDataProvider.MySqlDataProviderQueryConstractor.getGameParams())).executeQuery();
			while (result.next()){
				String currentValue = result.getString(2);
				switch(result.getString(1)){
				case SMALL_COIN_VALUE_NAME:
					gameParams.SmallCoinValue = currentValue;
					break;
				case MEDIUM_COIN_VALUE_NAME:
					gameParams.MediumCoinValue = currentValue;
					break;
				case BIG_COIN_VALUE_NAME:
					gameParams.BigCoinValue = currentValue;
					break;
				case ADD_COIN_BUFF_VALUE_NAME:
					gameParams.AddCoinBuffValue = currentValue;
					break;
				case ADD_COIN_BUFF_TIME_VALUE_NAME:
					gameParams.AddCoinBuffTimeValue = currentValue;
					break;
				case ADD_TIME_BUFF_VALUE_NAME:
					gameParams.AddTimeBuffValue = currentValue;
					break;
				case STOP_SHOOTING_BUFF_VALUE_NAME:
					gameParams.StopShootingBuffValue = currentValue;
					break;
				case UPDATE_ENERGY_FREQUENCY_NAME:
					gameParams.UpdateEnergyFrequencyValue = currentValue;
					break;
				}
			}
			result.close();
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		};
		return gameParams;
	}

	public PlayerParams getPlayerParams(PlayerAuthInfoParamsRq playerInfoParamsRq, float energyPerMinute) {
		PlayerParams playerParams = new PlayerParams();
		try{
			long playerIdLong = Long.parseLong(playerInfoParamsRq.PlayerId);
			Connection conn = source.getConnection();
			ResultSet result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerParams(conn, playerIdLong, energyPerMinute).executeQuery();
			if (!result.next()){
				result.close();
				String md5Hash = getMD5Hash(APPLICATION_ID + "_" + playerInfoParamsRq.PlayerId + "_" + SECRET_APPLICATION_KEY);
				if (md5Hash.equals(playerInfoParamsRq.AuthKey)){
					createNewPlayer(conn,playerIdLong,playerInfoParamsRq.AuthKey);
					result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerParams(conn, playerIdLong, energyPerMinute).executeQuery();
					result.next();
				}
			}
			if (result.getString(9).equals(playerInfoParamsRq.AuthKey)){
				playerParams.EffectVolumeValue = result.getString(1);
				playerParams.MusicVolumeValue = result.getString(2);
				playerParams.AddCoinBuffNumber = result.getString(3);
				playerParams.AddTimeBuffNumber = result.getString(4);
				playerParams.StopShootingBuffNumber = result.getString(5);
				playerParams.CoinNumber = result.getString(6);
				playerParams.CurrentAdventureId = result.getString(7);
				playerParams.Energy = result.getString(8);
				result.close();
				int newUID = generateUID();
				MySqlDataProvider.MySqlDataProviderQueryConstractor.createPlayerUIDs(conn, playerIdLong, newUID).executeUpdate();
				playerParams.UID = String.valueOf(newUID);
				MySqlDataProvider.MySqlDataProviderQueryConstractor.resetPlayerLevel(conn, playerIdLong).executeUpdate();
				
	            MySqlDataProvider.MySqlDataProviderQueryConstractor.updatePlayerEnergy(conn, playerIdLong, Integer.parseInt(playerParams.Energy)).executeUpdate();
				result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerLevels(conn, playerIdLong).executeQuery();
				String currentAdventureId = null;
				AdventureParams currentAdventureParams = null;
				LevelParams levelParams = null;
				while (result.next()){
					if (!result.getString(14).equals(currentAdventureId)){
						currentAdventureId = result.getString(14);
						currentAdventureParams = new AdventureParams();
						currentAdventureParams.AdventureId = currentAdventureId;
						currentAdventureParams.AdventureCost = result.getString(15);
						playerParams.Adventures.add(currentAdventureParams);
					}
					levelParams = new LevelParams();
					levelParams.Id = result.getString(1);
					levelParams.LevelId = result.getString(2);
					levelParams.LevelName = result.getString(3);
					levelParams.LevelDescription = result.getString(4);
					levelParams.PreviousLevelId = result.getString(5);
					levelParams.NextLevelId = result.getString(6);
					levelParams.SceneName = result.getString(7);
					levelParams.LevelTime = result.getString(8);
					levelParams.TwoStarTime = result.getString(9);
					levelParams.ThreeStarTime = result.getString(10);
					levelParams.EnergyCost = result.getString(11);
					if (result.getString(12) != null){
						levelParams.IsOpen = Boolean.toString(true);
						levelParams.BestResult = result.getString(12);
						levelParams.StarNumber = result.getString(13);
					}
					else {
						levelParams.IsOpen = Boolean.toString(false);
					}
					currentAdventureParams.Levels.add(levelParams);
				}
			}
			result.close();
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
		return playerParams;
	}
	
	public LevelStartedParamsRs setLevelStarted(String playerId, LevelStartedParamsRq levelStartedParams){
		LevelStartedParamsRs levelStartedParamsRs = new LevelStartedParamsRs();
		try{
			long playerIdLong = Long.parseLong(playerId);
			Connection conn = source.getConnection();
			if(MySqlDataProvider.MySqlDataProviderQueryConstractor.getLevelPlayerInfo(conn, playerIdLong, Long.parseLong(levelStartedParams.LevelPlayerId)).executeQuery().next()){
				ResultSet result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getLevelInfo(conn, levelStartedParams.LevelId).executeQuery();
				if (result.next()){
					int energyCost = result.getInt(10);
					result.close();
					result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerUIDAuthKeyWithEnergy(conn, playerIdLong, Integer.parseInt(levelStartedParams.UID), levelStartedParams.AuthKey).executeQuery();
					if (result.next()){
						int energy = result.getInt(1);
						if (energy >= energyCost){
							energy -= energyCost;
							MySqlDataProvider.MySqlDataProviderQueryConstractor.startLevel(conn, playerIdLong, Long.parseLong(levelStartedParams.LevelPlayerId), energy).executeUpdate();
							levelStartedParamsRs.Energy = Integer.toString(energy);
							result.close();
						}
					}
				}
			}
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
		return levelStartedParamsRs;
	}
	
	public LevelFinalizedParamsRs setLevelFinalized(String playerId, LevelFinalizedParamsRq levelFinalizedParams){
		LevelFinalizedParamsRs levelFinalizedParamsRs = new LevelFinalizedParamsRs();
		try{
			long playerIdLong = Long.parseLong(playerId);
			Connection conn = source.getConnection();
			ResultSet result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerUIDAuthKeyWithLevelTime(conn, playerIdLong, Integer.parseInt(levelFinalizedParams.UID), levelFinalizedParams.AuthKey).executeQuery();
			if(result.next()){
				long levelPlayerId = result.getLong(1);
				float levelPlayerTime = result.getFloat(2);
				result.close();
				if (levelPlayerId != 0 && levelPlayerId == Long.parseLong(levelFinalizedParams.LevelPlayerId)){
					result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getLevelInfo(conn, levelFinalizedParams.LevelId).executeQuery();
					if (result.next()){
						if(levelPlayerTime >= result.getFloat(11) && Integer.parseInt(levelFinalizedParams.CoinNumber) <= result.getInt(12)){
							MySqlDataProvider.MySqlDataProviderQueryConstractor.addCoin(conn, playerIdLong, Integer.parseInt(levelFinalizedParams.CoinNumber)).executeUpdate();
							int starNumber = 1;
							float levelTime = Float.parseFloat(levelFinalizedParams.LevelTime);
							if(levelTime >= result.getFloat(9)){
								starNumber = 3;
							}
							else if(levelTime >= result.getFloat(8)){
								starNumber = 2;
							}
							String nextLevelId = result.getString(5);
							result.close();
							result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getLevelPlayerInfo(conn, playerIdLong, Long.parseLong(levelFinalizedParams.LevelPlayerId)).executeQuery();
							if (result.next()){
								if(result.getFloat(2) < levelTime){
									MySqlDataProvider.MySqlDataProviderQueryConstractor.updateLevel(conn, Long.parseLong(levelFinalizedParams.LevelPlayerId), levelTime, starNumber).executeUpdate();
									levelFinalizedParamsRs.StarNumber = Integer.toString(starNumber);
								}
								else {
									levelFinalizedParamsRs.StarNumber = Integer.toString(result.getInt(3));
								}
								result.close();
							}
							if (!nextLevelId.equals(EMPTY_CONST)){
								result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getLevelPlayerInfo(conn, playerIdLong, nextLevelId).executeQuery();
								if (!result.next()){
									MySqlDataProvider.MySqlDataProviderQueryConstractor.createPlayerLevel(conn, nextLevelId, playerIdLong).executeUpdate();
									result.close();
									result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getLevelPlayerInfo(conn, playerIdLong, nextLevelId).executeQuery();
									result.next();
								}
								levelFinalizedParamsRs.NextLevelPlayerId = Integer.toString(result.getInt(1));
								result.close();
							}							
						}
					}
				}
			}
			else{
				result.close();
			}
			MySqlDataProvider.MySqlDataProviderQueryConstractor.resetPlayerLevel(conn, playerIdLong).executeUpdate();
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
		return levelFinalizedParamsRs;
	}
	
	public LevelResetParamsRs setLevelReset(String playerId, LevelResetParamsRq levelResetParams){
		try{
			long playerIdLong = Long.parseLong(playerId);
			Connection conn = source.getConnection();
			if(MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerUIDAuthKey(conn, playerIdLong, Integer.parseInt(levelResetParams.UID),levelResetParams.AuthKey).executeQuery().next()){
				MySqlDataProvider.MySqlDataProviderQueryConstractor.resetPlayerLevel(conn, playerIdLong).executeUpdate();
			}
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
		return new LevelResetParamsRs();
	}

	public EnergyParams updateEnergyParams(String playerId, float energyPerMinute){
		EnergyParams energyParams = new EnergyParams();
		try{
			Connection conn = source.getConnection();
			long playerIdLong = Long.parseLong(playerId);
			MySqlDataProvider.MySqlDataProviderQueryConstractor.updatePlayerEnergy(conn, playerIdLong, energyPerMinute).executeUpdate();
			ResultSet result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerEnergy(conn, playerIdLong).executeQuery();
			result.next();
			energyParams.Energy = result.getString(1);
			result.close();
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
		return energyParams;
	}
	
	public void reduceEnergy(String playerId, EnergyParamsRq energy){
		try{
			long playerIdLong = Long.parseLong(playerId);
			Connection conn = source.getConnection();
			if (MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerUIDAuthKey(conn, playerIdLong, Integer.parseInt(energy.UID), energy.AuthKey).executeQuery().next()){
				MySqlDataProvider.MySqlDataProviderQueryConstractor.reducePlayerEnergy(conn, playerIdLong, Math.abs(Integer.parseInt(energy.Energy))).executeUpdate();
			}
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
	}
	
	//new

	public void reduceAddCoinBuffNumber(String playerId, AddCoinBuffNumberParamsRq addCoinBuffNumberParams) {
		try{
			long playerIdLong = Long.parseLong(playerId);
			Connection conn = source.getConnection();
			if (MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerUIDAuthKey(conn, playerIdLong, Integer.parseInt(addCoinBuffNumberParams.UID), addCoinBuffNumberParams.AuthKey).executeQuery().next()){
				MySqlDataProvider.MySqlDataProviderQueryConstractor.reduceAddCoinBuffNumber(conn, playerIdLong, Math.abs(Integer.parseInt(addCoinBuffNumberParams.AddCoinBuffNumber))).executeUpdate();
			}
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}		
	}

	public void reduceAddTimeNumber(String playerId, AddTimeBuffNumberParamsRq addTimeBuffNumberParams) {
		try{
			long playerIdLong = Long.parseLong(playerId);
			Connection conn = source.getConnection();
			if (MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerUIDAuthKey(conn, playerIdLong, Integer.parseInt(addTimeBuffNumberParams.UID), addTimeBuffNumberParams.AuthKey).executeQuery().next()){
				MySqlDataProvider.MySqlDataProviderQueryConstractor.reduceAddTimeBuffNumber(conn, playerIdLong, Math.abs(Integer.parseInt(addTimeBuffNumberParams.AddTimeBuffNumber))).executeUpdate();
			}
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
	}

	public void reduceStopShootingBuffNumber(String playerId, StopShootingBuffNumberParamsRq stopShootingBuffNumberParams) {
		try{
			long playerIdLong = Long.parseLong(playerId);
			Connection conn = source.getConnection();
			if (MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerUIDAuthKey(conn, playerIdLong, Integer.parseInt(stopShootingBuffNumberParams.UID), stopShootingBuffNumberParams.AuthKey).executeQuery().next()){
				MySqlDataProvider.MySqlDataProviderQueryConstractor.reduceStopShootingBuffNumber(conn, playerIdLong, Math.abs(Integer.parseInt(stopShootingBuffNumberParams.StopShootingBuffNumber))).executeUpdate();
			}
			conn.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
	}
	
	public void changeCoinNumber(String playerId, CoinNumberParamsRq coinNumberParams) {
//		try{
//			long playerIdLong = Long.parseLong(playerId);
//			Connection conn = source.getConnection();
//			ResultSet result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerAddCoinUID(conn, playerIdLong).executeQuery();
//			result.next();
//			int coinUID = result.getInt(1);
//			if (getMD5Hash(CoinNumberParamsRq.getHashString(Integer.parseInt(coinNumberParams.CoinNumber), ++coinUID)).equals(coinNumberParams.Hash)){
//				MySqlDataProvider.MySqlDataProviderQueryConstractor.addCoin(conn, playerIdLong, Math.abs(Integer.parseInt(coinNumberParams.CoinNumber)), coinUID).executeUpdate();
//			}
//			result.close();
//			conn.close();
//		}
//		catch(Exception ex){
//			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
//		}
	}
	
	public void changeLevelData(String playerId, LevelDataParamsRq levelDataParams) {
//		try{
//			long playerIdLong = Long.parseLong(playerId);
//			Connection conn = source.getConnection();
//			ResultSet result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerLevelUID(conn, playerIdLong).executeQuery();
//			result.next();
//			int levelUID = result.getInt(1);
//			if (getMD5Hash(LevelDataParamsRq.getHashString(Integer.parseInt(levelDataParams.Id),levelDataParams.LevelId, ++levelUID)).equals(levelDataParams.Hash)){
//				MySqlDataProvider.MySqlDataProviderQueryConstractor.updateLevel(conn, Integer.parseInt(levelDataParams.Id), Float.parseFloat(levelDataParams.BestResult), Integer.parseInt(levelDataParams.StarNumber)).executeUpdate();
//				MySqlDataProvider.MySqlDataProviderQueryConstractor.updateLevelUID(conn, playerIdLong, levelUID).executeUpdate();
//			}
//			result.close();
//			conn.close();
//		}
//		catch(Exception ex){
//			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
//		}
	}
	
	public LevelParams openLevel(String playerId, OpenLevelParamsRq openLevelParams) {
//		LevelParams levelParams = new LevelParams();
//		try{
//			long playerIdLong = Long.parseLong(playerId);
//			Connection conn = source.getConnection();
//			ResultSet result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerOpenLevelUID(conn, playerIdLong).executeQuery();
//			result.next();
//			int openLevelUID = result.getInt(1);
//			if (getMD5Hash(OpenLevelParamsRq.getHashString(openLevelParams.AdventureId, openLevelParams.LevelId, ++openLevelUID)).equals(openLevelParams.Hash)){
//				result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getLevelTechId(conn, openLevelParams.LevelId).executeQuery();
//				result.next();
//				int newLevelId = result.getInt(1);
//				System.out.println(newLevelId);
//				MySqlDataProvider.MySqlDataProviderQueryConstractor.createPlayerLevel(conn, newLevelId, playerIdLong).executeUpdate();
//				MySqlDataProvider.MySqlDataProviderQueryConstractor.updateOpenLevelUID(conn, playerIdLong, openLevelUID).executeUpdate();
//				result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getPlayerLevelInfo(conn, playerIdLong, newLevelId).executeQuery();
//				result.next();
//				levelParams.Id = String.valueOf(result.getLong(1));
//				levelParams.LevelId = openLevelParams.LevelId;
//				levelParams.BestResult = String.valueOf(result.getFloat(2));
//				levelParams.StarNumber = String.valueOf(result.getInt(3));
//			}
//			result.close();
//			conn.close();
//		}
//		catch(Exception ex){
//			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
//		}
//		return levelParams;
		return null;
	}
	
	//end new
	
	private void createNewPlayer(Connection conn, long playerId, String playerAuthKey){
		try{
			MySqlDataProvider.MySqlDataProviderQueryConstractor.createPlayer(conn, playerId, playerAuthKey).executeUpdate();
			ResultSet result = MySqlDataProvider.MySqlDataProviderQueryConstractor.getLevelTechId(conn, FIRST_LEVEL_ID).executeQuery();
			result.next();
			MySqlDataProvider.MySqlDataProviderQueryConstractor.createPlayerLevel(conn, result.getInt(1), playerId).executeUpdate();
			result.close();
		}
		catch(Exception ex){
			MySqlDataProvider.logger.log(Level.SEVERE, "Exception: ", ex);
		}
	}
	
	private int generateUID(){
		return new Random(System.currentTimeMillis()).nextInt(MAX_UID);
	}
	
	private String getMD5Hash(String hashString)throws Exception{
        MessageDigest md5 = MessageDigest.getInstance("MD5");        
        StringBuffer  hexString = new StringBuffer();
        md5.reset();
        md5.update(hashString.getBytes());     
        byte messageDigest[] = md5.digest();            
        for (int i = 0; i < messageDigest.length; i++) {
            String hex=Integer.toHexString(0xFF & messageDigest[i]);
            if(hex.length()==1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
	}
	
	public static class MySqlDataProviderQueryConstractor{		
		public static PreparedStatement getPlayerParams(Connection conn, long playerId, float energyPerMinute) throws Exception{
			String query = String.format("select effect_volume_value,music_volume_value,add_coin_buff_number,add_time_buff_number,stop_shooting_buff_number,coin_number,current_adventure_id, greatest(0, least(floor((unix_timestamp(now()) - unix_timestamp(last_energy_update))/60 * %s + energy), 100)),auth_key from maniacrunnerdb.player where id = ? limit 1 ",  Float.toString(energyPerMinute));
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			return statement;
		}
		
		//Вот тут надо order by добавить
		public static PreparedStatement getPlayerLevels(Connection conn, long playerId) throws Exception{
			String query = "select l_pl.id,l.level_id,l.level_name,l.level_description,l.previous_level_id,l.next_level_id,l.scene_name,l.level_time_sec,l.time_two_star_sec,l.time_three_star_sec,l.energy_cost,l_pl.best_result,l_pl.star_number,ad.adventure_id,ad.adventure_cost from maniacrunnerdb.level l join maniacrunnerdb.adventure ad on l.adventure_id = ad.id left join (select * from maniacrunnerdb.level_player where player_id = ?) l_pl on l_pl.level_id = l.id order by ad.adventure_id asc";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			return statement;
		}		
//		public static String getPlayerAdventures(String playerId){
//			return String.format("select ad_pl.id, ad.adventure_id from maniacrunnerdb.adventure_player ad_pl inner join maniacrunnerdb.adventure ad on ad_pl.adventure_id = ad.id where ad_pl.player_id = %s", playerId);
//		}
//		public static String getPlayerLevels(String playerId){
//			return String.format("select l_pl.id,l.level_id,l_pl.best_result,l_pl.star_number from maniacrunnerdb.level_player l_pl inner join maniacrunnerdb.level l on l_pl.level_id = l.id where l_pl.player_id = %s", playerId);
//		}
		public static String getGameParams(){
			return String.format("select param_name,param_value from maniacrunnerdb.gameparams");
		}		
		public static PreparedStatement createPlayer(Connection conn, long playerId, String playerAuthKey) throws Exception{
			String query = "insert into maniacrunnerdb.player(id, auth_key) values(?,?)";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			statement.setString(2, playerAuthKey);
			return statement;
		}
//		public static String createPlayerAdventure(String playerId, String adventureId){
//			return String.format("insert into adventure_player(adventure_id,player_id) values(%s,%s)",
//					adventureId,
//					playerId);
//		}
		public static PreparedStatement createPlayerLevel(Connection conn, int level_id, long playerId) throws Exception{
			String query = "insert into maniacrunnerdb.level_player(level_id,player_id) values(?,?)";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, level_id);
			statement.setLong(2, playerId);
			return statement;
		}
		public static PreparedStatement createPlayerLevel(Connection conn, String level_id, long playerId) throws Exception{
			String query = "insert into maniacrunnerdb.level_player(level_id,player_id) select id,? from maniacrunnerdb.level where binary level_id = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			statement.setString(2, level_id);
			return statement;
		}
		public static PreparedStatement createPlayerUIDs(Connection conn, long playerId, int newUID) throws Exception {
			String query = String.format("update maniacrunnerdb.player set last_uid = %s where id = ?", newUID);
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			return statement;
		}
		
		public static PreparedStatement resetPlayerLevel(Connection conn, long playerId) throws Exception {
			String query = "update maniacrunnerdb.player set current_level_id = NULL, current_level_start_time = NULL where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			return statement;
		}
		
/*		public static PreparedStatement resetPlayerLevelUpdateCoin(Connection conn, long playerId, int coinNumber) throws Exception {
			String query = "update maniacrunnerdb.player set current_level_id = NULL, current_level_start_time = NULL, coin_number = ? where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, coinNumber);
			statement.setLong(2, playerId);
			return statement;
		}*/
		
		public static PreparedStatement getPlayerUID(Connection conn, long playerId, int uid) throws Exception{
			String query = "select id from maniacrunnerdb.player where id = ? and last_uid = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			statement.setInt(2, uid);
			return statement;
		}
		public static PreparedStatement getPlayerUIDAuthKey(Connection conn, long playerId, int uid, String authKey) throws Exception{
			String query = "select id from maniacrunnerdb.player where id = ? and last_uid = ? and binary auth_key = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			statement.setInt(2, uid);
			statement.setString(3, authKey);
			return statement;
		}
		
		public static PreparedStatement getPlayerUIDAuthKeyWithEnergy(Connection conn, long playerId, int uid, String authKey) throws Exception{
			String query = "select energy from maniacrunnerdb.player where id = ? and last_uid = ? and binary auth_key = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			statement.setInt(2, uid);
			statement.setString(3, authKey);
			return statement;
		}
		
		public static PreparedStatement getPlayerUIDAuthKeyWithLevelTime(Connection conn, long playerId, int uid, String authKey) throws Exception{
			String query = "select current_level_id, unix_timestamp(now()) - unix_timestamp(current_level_start_time) from maniacrunnerdb.player where id = ? and last_uid = ? and binary auth_key = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			statement.setInt(2, uid);
			statement.setString(3, authKey);
			return statement;
		}
		
/*		public static PreparedStatement getPlayerAddCoinUID(Connection conn, long playerId) throws Exception {
			String query = "select add_coin_uid from maniacrunnerdb.player where id = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			return statement;
		}
		public static PreparedStatement getPlayerLevelUID(Connection conn, long playerId) throws Exception {
			String query = "select level_uid from maniacrunnerdb.player where id = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			return statement;
		}
		public static PreparedStatement getPlayerOpenLevelUID(Connection conn, long playerId) throws Exception {
			String query = "select open_level_uid from maniacrunnerdb.player where id = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			return statement;
		}*/
		public static String getAdventureTechId(String adventureId){
			return String.format("select id from maniacrunnerdb.adventure where adventure_id = %s limit 1",
					adventureId);
		}
		public static PreparedStatement getLevelTechId(Connection conn, String levelId) throws Exception{
			String query = "select id from maniacrunnerdb.level where binary level_id = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, levelId);
			return statement;
		}
		public static PreparedStatement getPlayerEnergy(Connection conn, long playerId)throws Exception{
			String query = "select energy from maniacrunnerdb.player where id = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			return statement;
		}
		public static PreparedStatement reducePlayerEnergy(Connection conn, long playerId, int energyReduction) throws Exception{
			String query = "update maniacrunnerdb.player set energy = greatest(0,least(energy - ?,100)) where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, energyReduction);
			statement.setLong(2, playerId);
			return statement;
		}
		public static PreparedStatement updatePlayerEnergy(Connection conn, long playerId, int energy) throws Exception{
			String query = "update maniacrunnerdb.player set energy = greatest(0, least(?,100)), last_energy_update = now() where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, energy);
			statement.setLong(2, playerId);
			return statement;
		}
		public static PreparedStatement updatePlayerEnergy(Connection conn, long playerId, float energyPerMinute)throws Exception{
			String query = String.format("update maniacrunnerdb.player set energy = greatest(0, least(floor((unix_timestamp(now()) - unix_timestamp(last_energy_update))/60 * %s + energy), 100)), last_energy_update = now() where id = ?", String.valueOf(energyPerMinute));
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, playerId);
			return statement;
		}
		public static PreparedStatement reduceAddCoinBuffNumber(Connection conn, long playerId, int numberReduction) throws Exception{
			String query = "update maniacrunnerdb.player set add_coin_buff_number = greatest(0, add_coin_buff_number - ?)  where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, numberReduction);
			statement.setLong(2, playerId);
			return statement;
		}
		public static PreparedStatement reduceAddTimeBuffNumber(Connection conn, long playerId, int numberReduction) throws Exception{
			String query = "update maniacrunnerdb.player set add_time_buff_number = greatest(0, add_time_buff_number - ?)  where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, numberReduction);
			statement.setLong(2, playerId);
			return statement;
		}
		public static PreparedStatement reduceStopShootingBuffNumber(Connection conn, long playerId, int numberReduction) throws Exception{
			String query = "update maniacrunnerdb.player set stop_shooting_buff_number = greatest(0,stop_shooting_buff_number - ?)  where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, numberReduction);
			statement.setLong(2, playerId);
			return statement;
		}
		public static PreparedStatement addCoin(Connection conn, long playerId, int coinNumber) throws Exception{
			String query = "update maniacrunnerdb.player set coin_number = coin_number + ?  where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, coinNumber);
			statement.setLong(2, playerId);
			return statement;
		}
/*		public static PreparedStatement updateLevelUID(Connection conn, long playerId, int levelUID) throws Exception{
			String query = "update maniacrunnerdb.player set level_uid = ? where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, levelUID);
			statement.setLong(2, playerId);
			return statement;
		}
		public static PreparedStatement updateOpenLevelUID(Connection conn, long playerId, int openLevelUID) throws Exception{
			String query = "update maniacrunnerdb.player set open_level_uid = ? where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, openLevelUID);
			statement.setLong(2, playerId);
			return statement;
		}*/
		public static PreparedStatement updateLevel(Connection conn, long levelPlayerId, float bestResult, int starNumber) throws Exception{
			String query = "update maniacrunnerdb.level_player set best_result = ?, star_number = ?  where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setFloat(1, bestResult);
			statement.setInt(2, starNumber);
			statement.setLong(3, levelPlayerId);
			return statement;
		}
		
		public static PreparedStatement getLevelInfo(Connection conn, String levelId) throws Exception{
			String query = "select id,level_name,level_description,previous_level_id,next_level_id,scene_name,level_time_sec,time_two_star_sec,time_three_star_sec,energy_cost,min_time_sec,max_coin from maniacrunnerdb.level where level_id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, levelId);
			return statement;
		}
		
		public static PreparedStatement getLevelPlayerInfo(Connection conn, long playerId, int levelId) throws Exception{
			String query = "select id,best_result,star_number from maniacrunnerdb.level_player where level_id = ? and player_id = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setInt(1, levelId);
			statement.setLong(2, playerId);
			return statement;
		}
		
		public static PreparedStatement getLevelPlayerInfo(Connection conn, long playerId, long levelPlayerId) throws Exception{
			String query = "select level_id,best_result,star_number from maniacrunnerdb.level_player where id = ? and player_id = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, levelPlayerId);
			statement.setLong(2, playerId);
			return statement;
		}
		
		public static PreparedStatement getLevelPlayerInfo(Connection conn, long playerId, String levelId) throws Exception{
			String query = "select id,best_result,star_number from maniacrunnerdb.level_player where level_id = (select id from maniacrunnerdb.level where level_id = ? limit 1) and player_id = ? limit 1";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, levelId);
			statement.setLong(2, playerId);
			return statement;
		}
		
		//new ----------------------------
		public static PreparedStatement startLevel(Connection conn, long playerId, long levelId, int energy) throws Exception{
			String query = "update maniacrunnerdb.player set current_level_id = ?, current_level_start_time = now(), energy = ? where id = ?";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setLong(1, levelId);
			statement.setInt(2, energy);
			statement.setLong(3, playerId);
			return statement;
		}
	}

	@Override
	public void setPlayerSound(String playerId, SoundParamsRq soundParams) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlayerCurrentAdventureId(String playerId, CurrentAdventureIdParamsRq currentAdventureIdParams) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openAdventure(String playerId, OpenAdventureParamsRq openAdventureParams) {
		// TODO Auto-generated method stub
		
	}
}
