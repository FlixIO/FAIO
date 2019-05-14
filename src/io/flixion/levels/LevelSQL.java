package io.flixion.levels;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import io.flixion.main.FAIOPlugin;
import io.flixion.staffmode.StaffHandler;

public class LevelSQL {
	
	public static void updateFaction (String oldName, String newName) {
		Bukkit.getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement updateFaction = dbc.prepareStatement("UPDATE tblLevels SET uuid=? WHERE uuid=?");
					updateFaction.setString(1, newName);
					updateFaction.setString(2, oldName);
					updateFaction.executeUpdate();
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void getAllFactionLevelObjects () {
		final long initTime = System.currentTimeMillis();
		Bukkit.getServer().getScheduler().runTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String getAllFactions = "SELECT * FROM tblLevels";
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement getAllF = dbc.prepareStatement(getAllFactions);
					ResultSet rs = getAllF.executeQuery();
					int objects = 0;
					while (rs.next()) {
						HashMap<String, Integer> taskIDs = new HashMap<>();
						Levels.activeFactions.put(rs.getString(1), new FactionLevelObject(rs.getDouble(2), rs.getString(1), taskIDs, rs.getInt(3)));
						objects++;
					}
					dbc.close();
					for (Faction f : Factions.getInstance().getAllFactions()) {
						if (f.isWilderness() || f.isWarZone() || f.isSafeZone()) {
							continue;
						}
						if (!Levels.activeFactions.containsKey(f.getTag())) {
							HashMap<String, Integer> taskIDs = new HashMap<>();
							Levels.activeFactions.put(f.getTag(), new FactionLevelObject(0D, f.getTag(), taskIDs, 1));
							addFaction(f.getTag());
							objects++;
						}
					}
					FAIOPlugin.logConsoleMessage("Successfully cached " + objects + " Faction Objects in " + (System.currentTimeMillis() - initTime) + "ms");
				} catch (SQLException e) {
					FAIOPlugin.logConsoleError("Unable to cache FactionLevel Objects! Disabling plugin");
					Bukkit.getServer().getPluginManager().disablePlugin(FAIOPlugin.getInstance());
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void updateFactionLevel (String fID, int level) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String updateFaction = "UPDATE tblLevels SET level=? WHERE uuid=?";
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement updateF = dbc.prepareStatement(updateFaction);
					updateF.setInt(1, level);
					updateF.setString(2, fID);
					updateF.executeUpdate();
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void updateFactionEXP (String fID, Double exp) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String updateFaction = "UPDATE tblLevels SET exp=? WHERE uuid=?";
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement updateF = dbc.prepareStatement(updateFaction);
					updateF.setDouble(1, exp);
					updateF.setString(2, fID);
					updateF.executeUpdate();
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void removeFaction (String fID) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String removeFaction = "DELETE FROM tblLevels WHERE uuid=?";
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement removeF = dbc.prepareStatement(removeFaction);
					removeF.setString(1, fID);
					removeF.execute();
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void addFaction(String fID) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String addFaction = "INSERT INTO tblLevels VALUES (?,?,?)";
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement addF = dbc.prepareStatement(addFaction);
					addF.setString(1, fID);
					addF.setDouble(2, 0D);
					addF.setInt(3, 1);
					addF.execute();
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void initTblLevels() {
		Bukkit.getServer().getScheduler().runTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String createTbl = "CREATE TABLE IF NOT EXISTS tblLevels (uuid VARCHAR(36), exp DOUBLE (100,00000), level INT (2)) ENGINE=InnoDB DEFAULT CHARSET=latin1";
				try {
					StaffHandler.initStaff();
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement createT = dbc.prepareStatement(createTbl);
					createT.execute();
					dbc.close();
					getAllFactionLevelObjects();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
