package io.flixion.misc;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scheduler.BukkitTask;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;
import io.flixion.scoreboard.ScoreboardHandler;

public class Cooldowns implements Listener {
	private HashMap<UUID, Long> enderpearlCooldowns = new HashMap<>();
	private HashMap<UUID, Long> regularAppleCooldowns = new HashMap<>();
	private HashMap<UUID, Long> godAppleCooldowns = new HashMap<>();
	private HashMap<UUID, BukkitTask> enderpearlScoreboardTimerTasks = new HashMap<>();
	private BukkitTask task;
	private int enderpearlTime;
	private int regularAppleTime;
	private int superAppleTime;
	
	public Cooldowns(int enderpearlTime, int regularAppleTime, int superAppleTime) {
		super();
		this.enderpearlTime = enderpearlTime;
		this.regularAppleTime = regularAppleTime;
		this.superAppleTime = superAppleTime;
	}
	
	public boolean checkPearlCooldown (Player p) {
		if (enderpearlCooldowns.containsKey(p.getUniqueId())) {
			if (System.currentTimeMillis() - enderpearlCooldowns.get(p.getUniqueId()) <= enderpearlTime * 1000) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public boolean checkRegularAppleCooldown (Player p) {
		if (regularAppleCooldowns.containsKey(p.getUniqueId())) {
			if (System.currentTimeMillis() - regularAppleCooldowns.get(p.getUniqueId()) <= regularAppleTime * 1000) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public boolean checkSuperAppleCooldown (Player p) {
		if (godAppleCooldowns.containsKey(p.getUniqueId())) {
			if (System.currentTimeMillis() - godAppleCooldowns.get(p.getUniqueId()) <= superAppleTime * 1000) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void throwEnderpearl (ProjectileLaunchEvent e) {
		if (e.getEntityType() == EntityType.ENDER_PEARL) {
			if (e.getEntity().getShooter() instanceof Player) {
				Player p = (Player) e.getEntity().getShooter();
				if (checkPearlCooldown(p)) {
					e.setCancelled(true);
					p.sendMessage(Utils.cc("&4&l(!) You cannot do this for another &a" + (enderpearlTime - ((System.currentTimeMillis() - enderpearlCooldowns.get(p.getUniqueId()))/1000)) + " seconds"));
				}
				else {
					enderpearlCooldowns.put(p.getUniqueId(), System.currentTimeMillis());
					if (FAIOPlugin.scoreboardEnabled) {
						ScoreboardHandler.playerScoreboards.get(p.getUniqueId()).initEnderpearlTimerScoreboard();
					}
					task = Bukkit.getScheduler().runTaskTimer(FAIOPlugin.getInstance(), new Runnable() {
						int count = 0;
						@Override
						public void run() {
							if (count == enderpearlTime) {
								if (FAIOPlugin.scoreboardEnabled) {
									ScoreboardHandler.playerScoreboards.get(p.getUniqueId()).removeEnderpearlTimerScoreboard();
								}
								enderpearlScoreboardTimerTasks.get(p.getUniqueId()).cancel();
								enderpearlScoreboardTimerTasks.remove(p.getUniqueId());
							}
							else {
								if (FAIOPlugin.scoreboardEnabled) {
									ScoreboardHandler.playerScoreboards.get(p.getUniqueId()).setEnderpearlTimerScoreboard(enderpearlTime - count);
								}
								count++;
							}
						}
					}, 0, 20);
					enderpearlScoreboardTimerTasks.put(p.getUniqueId(), task);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void consumeApple (PlayerItemConsumeEvent e) {
		if (e.getItem().getType() == Material.GOLDEN_APPLE) {
			if (e.getItem().getDurability() == (short) 0) {
				if (checkRegularAppleCooldown(e.getPlayer())) {
					e.setCancelled(true);
					e.getPlayer().updateInventory();
					e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot do this for another &a" + (regularAppleTime - ((System.currentTimeMillis() - regularAppleCooldowns.get(e.getPlayer().getUniqueId()))/1000)) + " seconds"));
				}
				else {
					regularAppleCooldowns.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
				}
			}
			else if (e.getItem().getDurability() == (short) 1) {
				if (checkSuperAppleCooldown(e.getPlayer())) {
					e.setCancelled(true);
					e.getPlayer().updateInventory();
					e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot do this for another &a" + (superAppleTime - ((System.currentTimeMillis() - godAppleCooldowns.get(e.getPlayer().getUniqueId()))/1000)) + " seconds"));
				}
				else {
					godAppleCooldowns.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
				}
			}
		}
	}
}
