package io.flixion.combatlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;
import io.flixion.scoreboard.ScoreboardHandler;
import io.flixion.staffmode.StaffHandler;

public class LogHandler implements Listener{
	public static HashMap<UUID, CombatLogger> combatLoggers = new HashMap<>();
	public static HashMap<UUID, BukkitTask> inCombat = new HashMap<>();
	private ArrayList<UUID> punishOnJoin = new ArrayList<>();
	private String loggerNameFormat;
	private int tagTime;
	private BukkitTask combatTask;
	private ArrayList<String> blockedCommands;
	
	public LogHandler(String loggerNameFormat, int tagTime, ArrayList<String> blockedCommands) {
		super();
		this.loggerNameFormat = loggerNameFormat;
		this.tagTime = tagTime;
		this.blockedCommands = blockedCommands;
	}

	public void createLogger (Player p) {
		Villager v = (Villager) p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
		v.setAgeLock(true);
		v.setAdult();
		v.setBreed(false);
		v.setCanPickupItems(false);
		v.setCustomName(Utils.cc(loggerNameFormat).replaceAll("%playerName%", p.getName()));
		v.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 126));
		BukkitTask task = Bukkit.getScheduler().runTaskTimer(FAIOPlugin.getInstance(), new Runnable() {
			int count = 30;
			@Override
			public void run() {
				if (count == 0) {
					for (Map.Entry<UUID, CombatLogger> entry : combatLoggers.entrySet()) {
						if (entry.getValue().getEntityUUID().equals(v.getUniqueId())) {
							entry.getValue().getV().remove();
							entry.getValue().getTask().cancel();
							combatLoggers.remove(entry.getKey());
						}
					}
				}
				else {
					v.setCustomName(Utils.cc(loggerNameFormat + " &a(" + count + ")").replaceAll("%playerName%", p.getName()));
				}
				count--;
			}
		}, 0, 20);
		CombatLogger cl = new CombatLogger(p.getUniqueId(), v.getUniqueId(), p.getInventory().getContents(), p.getInventory().getArmorContents(), p.getLevel(), v, task);
		combatLoggers.put(p.getUniqueId(), cl);
	}
	
	@EventHandler (ignoreCancelled=true)
	public void punishLoggerOnJoin (PlayerJoinEvent e) {
		if (combatLoggers.containsKey(e.getPlayer().getUniqueId())) {
			CombatLogger cl = combatLoggers.get(e.getPlayer().getUniqueId());
			cl.getTask().cancel();
			cl.getV().remove();
			combatLoggers.remove(e.getPlayer().getUniqueId());
		}
		else if (punishOnJoin.contains(e.getPlayer().getUniqueId())) {
			Player p = e.getPlayer();
			if (inCombat.containsKey(p.getUniqueId())) {
				inCombat.get(p.getUniqueId()).cancel();
				inCombat.remove(p.getUniqueId());
			}
			p.setLevel(0);
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.setHealth(0);
			punishOnJoin.remove(p.getUniqueId());
		}
	}
	
	@EventHandler
	public void cancelCombatTagOnDeath (PlayerDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (inCombat.containsKey(p.getUniqueId())) {
				inCombat.get(p.getUniqueId()).cancel();
				inCombat.remove(p.getUniqueId());
				if (FAIOPlugin.scoreboardEnabled) {
					if (ScoreboardHandler.playerScoreboards.containsKey(p.getUniqueId())) {
						ScoreboardHandler.playerScoreboards.get(p.getUniqueId()).removeCombatTimerScoreboard();
					}
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void killLogger (EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.VILLAGER) {
			for (Map.Entry<UUID, CombatLogger> entry : combatLoggers.entrySet()) {
				if (entry.getValue().getEntityUUID().equals(e.getEntity().getUniqueId())) {
					for (ItemStack i : entry.getValue().getPlayerInventory()) {
						if (Utils.notNull(i)) {
							if (i.getType() != Material.AIR) {
								e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), i);
							}
						}
					}
					for (ItemStack i : entry.getValue().getArmorInventory()) {
						if (Utils.notNull(i)) {
							if (i.getType() != Material.AIR) {
								e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), i);
							}
						}
					}
					e.setDroppedExp((int) entry.getValue().getExp());
					entry.getValue().getTask().cancel();
					punishOnJoin.add(entry.getKey());
					combatLoggers.remove(entry.getKey());
					break;
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void logInCombat(PlayerQuitEvent e) {
		if (inCombat.containsKey(e.getPlayer().getUniqueId())) {
			createLogger(e.getPlayer());
		}
	}
	
	@EventHandler
	public void initCombat (EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (e.isCancelled()) {
				return;
			}
			Player attacker = (Player) e.getDamager();
			Player defender = (Player) e.getEntity();
			if (StaffHandler.inStaffMode.containsKey(attacker.getUniqueId())) {
				return;
			}
			if (StaffHandler.frozenPlayers.containsKey(defender.getUniqueId())) {
				return;
			}
			if (inCombat.containsKey(attacker.getUniqueId())) {
				inCombat.get(attacker.getUniqueId()).cancel();
			}
			if (FAIOPlugin.scoreboardEnabled) {
				ScoreboardHandler.playerScoreboards.get(attacker.getUniqueId()).initCombatTimerScoreboard();
			}
			combatTask = Bukkit.getServer().getScheduler().runTaskTimer(FAIOPlugin.getInstance(), new Runnable() {
				int count = tagTime;
				@Override
				public void run() {
					if (count == 0) {
						if (inCombat.containsKey(attacker.getUniqueId())) {
							inCombat.get(attacker.getUniqueId()).cancel();
							inCombat.remove(attacker.getUniqueId());
							if (attacker.isOnline()) {
								if (FAIOPlugin.scoreboardEnabled) {
									ScoreboardHandler.playerScoreboards.get(attacker.getUniqueId()).removeCombatTimerScoreboard();
								}
							}
						}
					}
					else {
						if (attacker.isOnline()) {
							if (FAIOPlugin.scoreboardEnabled) {
								ScoreboardHandler.playerScoreboards.get(attacker.getUniqueId()).setCombatTimerScoreboard(count);
							}
						}
					}
					count--;
				}
				
			}, 0, 20);
			inCombat.put(attacker.getUniqueId(), combatTask);
			if (inCombat.containsKey(defender.getUniqueId())) {
				inCombat.get(defender.getUniqueId()).cancel();
			}
			if (FAIOPlugin.scoreboardEnabled) {
				ScoreboardHandler.playerScoreboards.get(defender.getUniqueId()).initCombatTimerScoreboard();
			}
			combatTask = Bukkit.getServer().getScheduler().runTaskTimer(FAIOPlugin.getInstance(), new Runnable() {
				int count = tagTime;
				@Override
				public void run() {
					if (count == 0) {
						if (inCombat.containsKey(defender.getUniqueId())) {
							inCombat.get(defender.getUniqueId()).cancel();
							inCombat.remove(defender.getUniqueId());
							if (defender.isOnline()) {
								if (FAIOPlugin.scoreboardEnabled) {
									ScoreboardHandler.playerScoreboards.get(defender.getUniqueId()).removeCombatTimerScoreboard();
								}
							}
						}
					}
					else {
						if (defender.isOnline()) {
							if (FAIOPlugin.scoreboardEnabled) {
								ScoreboardHandler.playerScoreboards.get(defender.getUniqueId()).setCombatTimerScoreboard(count);
							}
						}
					}
					count--;
				}
				
			}, 0, 20);
			inCombat.put(defender.getUniqueId(), combatTask);
		}
	}
	
	@EventHandler
	public void cancelCombatCommands (PlayerCommandPreprocessEvent e) {
		if (inCombat.containsKey(e.getPlayer().getUniqueId())) {
			if (e.getMessage().replaceAll("/", "").contains(" ")) {
				String command = e.getMessage().replaceAll("/", "").split(" ")[0];
				if (blockedCommands.contains(command.toLowerCase())) {
					e.setCancelled(true);
					e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot use this command in combat"));
				}
			}
			else {
				if (blockedCommands.contains(e.getMessage().toLowerCase().replace("/", ""))) {
					e.setCancelled(true);
					e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot use this command in combat"));
				}
			}
		}
	}
}
