package io.flixion.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;
import subside.plugins.koth.events.KothEndEvent;
import subside.plugins.koth.events.KothStartEvent;

public class KothHookEvents implements Listener{
	public static HashMap<UUID, BukkitTask> kothRunnables = new HashMap<>();
	private BukkitTask task;
	
	@EventHandler (ignoreCancelled=true)
	public void kothBegin (KothStartEvent e) {
		if (!FAIOPlugin.scoreboardEnabled) {
			return;
		}
		for (Map.Entry<UUID, PlayerScoreboard> entry : ScoreboardHandler.playerScoreboards.entrySet()) {
			if (Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())).length() > 16) {
				entry.getValue().getKothHandler().setPrefix(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())).substring(0, 17));
				entry.getValue().getKothHandler().addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())).substring(17, Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())).length()));
				entry.getValue().getPlayerScoreboardObjective().getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())).substring(17, Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())).length())).setScore(6);
			} else {
				entry.getValue().getKothHandler().addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())));
				entry.getValue().getPlayerScoreboardObjective().getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName()))).setScore(6);
			}
			task = Bukkit.getScheduler().runTaskTimer(FAIOPlugin.getInstance(), new Runnable() {
				Player p = Bukkit.getPlayer(entry.getKey());
				@Override
				public void run() {
					if (p.isOnline()) {
						entry.getValue().getKothHandler().setSuffix(Utils.cc(" &f" + e.getKoth().getKothHandler().getRunningKoth().getTimeObject().getTimeLeftFormatted()));
					}
				}
			}, 0, 20);
			kothRunnables.put(entry.getKey(), task);
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void kothEnd (KothEndEvent e) {
		if (!FAIOPlugin.scoreboardEnabled) {
			return;
		}
		for (Map.Entry<UUID, PlayerScoreboard> entry : ScoreboardHandler.playerScoreboards.entrySet()) {
			if (kothRunnables.containsKey(entry.getKey())) {
				kothRunnables.get(entry.getKey()).cancel();
			}
			if (Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())).length() > 16) {
				entry.getValue().getPlayerScoreboard().resetScores(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())).substring(17, Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())).length()));
			} else {
				entry.getValue().getPlayerScoreboard().resetScores(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", e.getKoth().getName())));
			}
		}
	}
}
