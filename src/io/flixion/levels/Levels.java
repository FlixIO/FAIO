package io.flixion.levels;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.FactionRenameEvent;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;

public class Levels implements Listener{
	public static String broadcastFactionUpgradeNotification;
	public static HashMap<String, FactionLevelObject> activeFactions = new HashMap<>();
	public static double expPerMinute;
	private static HashMap<Integer, Double> upgradeCosts;
	int taskID;
	
	public Levels (double expPerMinute, HashMap<Integer, Double> upgradeCosts, String broadcastFactionUpgradeNotification) {
		Levels.expPerMinute = expPerMinute;
		Levels.upgradeCosts = upgradeCosts;
		Levels.broadcastFactionUpgradeNotification = broadcastFactionUpgradeNotification;
		initPersist();
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler (ignoreCancelled=true)
	public void updateFaction (FactionRenameEvent e) {
		FactionLevelObject f = activeFactions.get(e.getOldFactionTag());
		activeFactions.put(e.getFactionTag(), new FactionLevelObject(f.getEXP(), e.getFactionTag(), f.getGeneratingPlayerTaskIDs(), f.getLevel()));
		activeFactions.remove(e.getOldFactionTag());
		LevelSQL.updateFaction(e.getOldFactionTag(), e.getFactionTag());
	}
	
	@EventHandler (ignoreCancelled=true)
	public void cancelGuiClick (InventoryClickEvent e) {
		if (e.getClickedInventory() != null) {
			if (e.getWhoClicked() instanceof Player) {
				if (e.getInventory().getName().equals(Utils.cc("&4&lFaction Levels"))) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void playerJoinServer (PlayerJoinEvent e) {
		FPlayer p = FPlayers.getInstance().getByPlayer(e.getPlayer());
		if (!p.getFaction().isWilderness() && !p.getFaction().isSafeZone() && !p.getFaction().isWarZone()) {
			if (activeFactions.get(p.getFaction().getTag()).getLevel() < 10) {
				activeFactions.get(p.getFaction().getTag()).getGeneratingPlayerTaskIDs().put(p.getAccountId(), initGenExpTask(p));
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void playerLeaveServer (PlayerQuitEvent e) {
		FPlayer p = FPlayers.getInstance().getByPlayer(e.getPlayer());
		if (!p.getFaction().isWilderness() && !p.getFaction().isSafeZone() && !p.getFaction().isWarZone()) {
			if (activeFactions.get(p.getFaction().getTag()).getLevel() < 10) {
				Bukkit.getScheduler().cancelTask(activeFactions.get(p.getFaction().getTag()).getGeneratingPlayerTaskIDs().get(p.getAccountId()));
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void playerLeaveFaction (FPlayerLeaveEvent e) {
		if (activeFactions.containsKey(e.getFaction().getTag())) {
			if (activeFactions.get(e.getFaction().getTag()).getGeneratingPlayerTaskIDs().containsKey(e.getfPlayer().getAccountId())){
				Bukkit.getScheduler().cancelTask(activeFactions.get(e.getFaction().getTag()).getGeneratingPlayerTaskIDs().get(e.getfPlayer().getAccountId()));
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void playerJoinFaction (FPlayerJoinEvent e) {
		if (activeFactions.get(e.getFaction().getTag()).getLevel() < 10) {
			activeFactions.get(e.getFaction().getTag()).getGeneratingPlayerTaskIDs().put(e.getfPlayer().getAccountId(), initGenExpTask(e.getfPlayer()));
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void factionCreate(FactionCreateEvent e) {
		FactionLevelObject f = new FactionLevelObject(0D, e.getFPlayer().getFactionId(), null, 1);
		LevelSQL.addFaction(e.getFactionTag());
		activeFactions.put(e.getFactionTag(), f);
		HashMap<String, Integer> playerTaskIDs = new HashMap<>();
		f.setGeneratingPlayerTaskIDs(playerTaskIDs);
	}
	
	@EventHandler (ignoreCancelled=true)
	public void factionDisband (FactionDisbandEvent e) {
		for (Player p : e.getFaction().getOnlinePlayers()) {
			FPlayer fp = FPlayers.getInstance().getByPlayer(p);
			Bukkit.getScheduler().cancelTask(activeFactions.get(fp.getFaction().getTag()).getGeneratingPlayerTaskIDs().get(fp.getAccountId()));
		}
		activeFactions.remove(e.getFaction().getTag());
		LevelSQL.removeFaction(e.getFaction().getTag());
	}
	
	@SuppressWarnings("deprecation")
	public Integer initGenExpTask(FPlayer p) {
		taskID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				if (!p.getFaction().isNone() && !p.getFaction().isSafeZone() && !p.getFaction().isWarZone()) {
					if (activeFactions.get(p.getFaction().getTag()).getLevel() < 10) {
						activeFactions.get(p.getFaction().getTag()).setEXP(activeFactions.get(p.getFaction().getTag()).getEXP() + expPerMinute);
						if (upgradeCosts.get(activeFactions.get(p.getFaction().getTag()).getLevel() + 1) <= activeFactions.get(p.getFaction().getTag()).getEXP()) {
							activeFactions.get(p.getFaction().getTag()).setLevel(activeFactions.get(p.getFaction().getTag()).getLevel() + 1);
							Bukkit.broadcastMessage(Utils.cc(broadcastFactionUpgradeNotification).replaceAll("%faction%", p.getFaction().getTag()).replaceAll("%level%", activeFactions.get(p.getFaction().getTag()).getLevel() + ""));
							
						}
					}
					else {
						Bukkit.getScheduler().cancelTask(activeFactions.get(p.getFaction().getTag()).getGeneratingPlayerTaskIDs().get(p.getAccountId()));
					}
				}
				else {
					Bukkit.getScheduler().cancelTask(activeFactions.get(p.getFaction().getTag()).getGeneratingPlayerTaskIDs().get(p.getAccountId()));
				}
			}
			
		}, 40, 20 * 60);
		return taskID;
	}
	
	public void initPersist () {
		Bukkit.getScheduler().runTaskTimerAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				for (Faction f : Factions.getInstance().getAllFactions()) {
					if (f.isWilderness() || f.isWarZone() || f.isSafeZone()) {
						continue;
					}
					if (activeFactions.get(f.getTag()).getLevel() < 10) {
						LevelSQL.updateFactionEXP(f.getTag(), activeFactions.get(f.getTag()).getEXP());
						LevelSQL.updateFactionLevel(f.getTag(), activeFactions.get(f.getTag()).getLevel());
					}
				}
			}
		}, 300 * 20, 60 * 30 * 20); //runs every 30 minutes
	}

	public static HashMap<Integer, Double> getUpgradeCosts() {
		return upgradeCosts;
	}
}
