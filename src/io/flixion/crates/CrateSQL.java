package io.flixion.crates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.flixion.data.PlayerHandler;
import io.flixion.data.PlayerProfile;
import io.flixion.main.FAIOPlugin;

public class CrateSQL {
	
	public static HashMap<UUID, Integer> keys = new HashMap<>();
	
	public static void dropTable (String crateName) {
		Bukkit.getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String dropTable = "ALTER TABLE tblCrates DROP COLUMN " + crateName;
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement dropT = dbc.prepareStatement(dropTable);
					dropT.execute();
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					
				}
			}
		});
	}
	
	public static void updatePlayerKeys(Player p, String crateName) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String updateKeys = "UPDATE tblCrates SET " + crateName + "=? WHERE uuid=?";
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement updateK = dbc.prepareStatement(updateKeys);
					updateK.setInt(1, PlayerHandler.getPlayerData().get(p.getUniqueId()).getCrateKeys().get(crateName));
					updateK.setString(2, p.getUniqueId().toString());
					updateK.executeUpdate();
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void getPlayerKeys(Player p) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			@Override
			public void run() {
				String getKeys = "SELECT * FROM tblCrates WHERE uuid=?";
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement getK = dbc.prepareStatement(getKeys);
					getK.setString(1, p.getUniqueId().toString());
					ResultSet rs = getK.executeQuery();
					HashMap<String, Integer> crateKeyProfile = new HashMap<>();
					while (rs.next()) {
						for (int i = 1; i < FAIOPlugin.crateNames.size() + 1; i++) {
							crateKeyProfile.put(FAIOPlugin.crateNames.get(i - 1), rs.getInt(i + 1));
						}
					}
					if (PlayerHandler.getPlayerData().containsKey(p.getUniqueId())) {
						PlayerHandler.getPlayerData().get(p.getUniqueId()).setCrateKeys(crateKeyProfile);
					}
					else {
						PlayerHandler.getPlayerData().put(p.getUniqueId(), new PlayerProfile(p.getUniqueId(), crateKeyProfile));
					}
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void insertCheckPlayer (Player p) {
		Bukkit.getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String checkPlayer = "SELECT * FROM tblCrates WHERE uuid=?";
				String insertPlayer = "INSERT INTO tblCrates VALUES(?,";
				for (int i = 0; i < FAIOPlugin.crateNames.size(); i++) {
					if (i == FAIOPlugin.crateNames.size() - 1) {
						insertPlayer += "?)";
					}
					else {
						insertPlayer += "?,";
					}
				}
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement checkP = dbc.prepareStatement(checkPlayer);
					checkP.setString(1, p.getUniqueId().toString());
					ResultSet rs = checkP.executeQuery();
					if (!rs.next()){
						PreparedStatement insertP = dbc.prepareStatement(insertPlayer);
						insertP.setString(1, p.getUniqueId().toString());
						for (int i = 2; i < FAIOPlugin.crateNames.size() + 2; i++) {
							insertP.setInt(i, 0);
						}
						insertP.execute();
					}
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void appendCratesTbl(String crateName) {
		Bukkit.getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String appendTblCrates = "ALTER TABLE tblCrates ADD " + crateName + " INT (4) DEFAULT '0'";
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement appendTbl = dbc.prepareStatement(appendTblCrates);
					appendTbl.execute();
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					
				}
			}
		});
	}
	
	public static void initCratesTbl(ArrayList<String> activeCrates) {
		Bukkit.getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String createTblCrates = "CREATE TABLE IF NOT EXISTS tblCrates (uuid VARCHAR(36)";
				for (int i = 0; i < activeCrates.size(); i++) {
					if (i == activeCrates.size() - 1) {
						createTblCrates += ", " + activeCrates.get(i) + " INT(4) DEFAULT '0'";
					}
					else {
						createTblCrates += ", " + activeCrates.get(i) + " INT(4) DEFAULT '0'";
					}
				}
				createTblCrates += ") ENGINE=InnoDB DEFAULT CHARSET=latin1";
				try {
					Connection dbc = FAIOPlugin.getHikariSource().getConnection();
					PreparedStatement checkTblExists = dbc.prepareStatement(createTblCrates);
					checkTblExists.execute();
					dbc.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
