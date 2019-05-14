package io.flixion.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;

public class ScoreboardHandler implements Listener{
	public static HashMap<UUID, PlayerScoreboard> playerScoreboards = new HashMap<>();
	public static String scoreboardTitle;
	public static HashMap<String, String> scorePrefixes;
	
	public ScoreboardHandler(String title, HashMap<String, String> scorePrefixes) {
		ScoreboardHandler.scoreboardTitle = title;
		ScoreboardHandler.scorePrefixes = scorePrefixes;
	}
	
	@EventHandler (ignoreCancelled=true)
	public void registerScoreboard (PlayerJoinEvent e) {
		playerScoreboards.put(e.getPlayer().getUniqueId(), new PlayerScoreboard(e.getPlayer()));
		for (Map.Entry<UUID, PlayerScoreboard> entry : playerScoreboards.entrySet()) {
			entry.getValue().getOnlinePlayers().setSuffix(Utils.cc(" &f" + Bukkit.getOnlinePlayers().size()));
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void unregisterScoreboard (PlayerQuitEvent e) {
		playerScoreboards.get(e.getPlayer().getUniqueId()).getPlayerScoreboardObjective().unregister();
		playerScoreboards.remove(e.getPlayer().getUniqueId());
		Bukkit.getScheduler().runTaskLater(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				for (Map.Entry<UUID, PlayerScoreboard> entry : playerScoreboards.entrySet()) {
					entry.getValue().getOnlinePlayers().setSuffix(Utils.cc(" &f" + Bukkit.getOnlinePlayers().size()));
				}
			}
		}, 20);
	}
	
	@EventHandler (ignoreCancelled=true)
	public void playerJoinFaction (FPlayerJoinEvent e) { 
		playerScoreboards.get(e.getfPlayer().getPlayer().getUniqueId()).getPlayerFaction().setSuffix(Utils.cc(" &f" + e.getFaction().getTag()));
	}
	
	@EventHandler (ignoreCancelled=true)
	public void playerLeaveFaction (FPlayerLeaveEvent e) {
		playerScoreboards.get(UUID.fromString(e.getfPlayer().getAccountId())).getPlayerFaction().setSuffix(Utils.cc(" &fWilderness"));
	}
}
