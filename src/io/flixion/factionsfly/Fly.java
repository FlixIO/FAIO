package io.flixion.factionsfly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;

import io.flixion.combatlog.LogHandler;
import io.flixion.data.PlayerHandler;
import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;
import io.flixion.scoreboard.ScoreboardHandler;
import io.flixion.staffmode.StaffHandler;

public class Fly implements Listener {
	private HashMap<UUID, BukkitTask> flyRunnables = new HashMap<>();
	private HashMap<UUID, BukkitTask> cancelFallDamage = new HashMap<>();
	public static ArrayList<UUID> isFlying = new ArrayList<>();
	private BukkitTask task;
	private int disableWhenNearbyEnemyBlocks = 10;
	private String nearbyEnemyWhileFlyingText;
	private String leaveOwnTerritoryWhileFlyingText;
	private String inCombatWhileFlyingText;

	public Fly(int disableWhenNearbyEnemyBlocks, String nearbyEnemyWhileFlyingText,
			String leaveOwnTerritoryWhileFlyingText, String inCombatWhileFlyingText) {
		super();
		this.disableWhenNearbyEnemyBlocks = disableWhenNearbyEnemyBlocks;
		this.nearbyEnemyWhileFlyingText = nearbyEnemyWhileFlyingText;
		this.leaveOwnTerritoryWhileFlyingText = leaveOwnTerritoryWhileFlyingText;
		this.inCombatWhileFlyingText = inCombatWhileFlyingText;
	}
	
	@EventHandler
	public void playerTeleportHandler (PlayerTeleportEvent e) {
		disableFlySync(e.getPlayer());
	}
	
	@EventHandler
	public void playerChangeWorldHandler (PlayerChangedWorldEvent e) {
		disableFlySync(e.getPlayer());
	}

	@EventHandler (ignoreCancelled=true)
	public void removeFlyRunnable(PlayerQuitEvent e) {
		if (flyRunnables.containsKey(e.getPlayer().getUniqueId())) {
			flyRunnables.get(e.getPlayer().getUniqueId()).cancel();
			flyRunnables.remove(e.getPlayer().getUniqueId());
			if (isFlying.contains(e.getPlayer().getUniqueId())) {
				isFlying.remove(e.getPlayer().getUniqueId());
			}
		}
	}

	@EventHandler (ignoreCancelled=true)
	public void changeGamemode(PlayerGameModeChangeEvent e) {
		if (isFlying.contains(e.getPlayer().getUniqueId())) {
			isFlying.remove(e.getPlayer().getUniqueId());
		}
	}

	@EventHandler (ignoreCancelled=true)
	public void overrideFlyCommand(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().equalsIgnoreCase("/fly")) {
			e.setCancelled(true);
		}
		if (e.getMessage().equalsIgnoreCase("/f stealth")) {
			if (PlayerHandler.getPlayerData().get(e.getPlayer().getUniqueId()).isfStealth()) {
				PlayerHandler.getPlayerData().get(e.getPlayer().getUniqueId()).setfStealth(false);
				e.getPlayer()
						.sendMessage(Utils.cc("&4&l(!) &eF Stealth has been disabled! Nearby enemies WILL lose fly!"));
			} else {
				PlayerHandler.getPlayerData().get(e.getPlayer().getUniqueId()).setfStealth(true);
				e.getPlayer()
						.sendMessage(Utils.cc("&4&l(!) &eF Stealth has been enabled! Nearby enemies will NOT lose fly!"));
			}
			e.setCancelled(true);
		}
	}

	public boolean nearbyEnemy(Player p, FPlayer fp) {
		boolean nearbyEnemy = false;
		for (Entity e1 : p.getPlayer().getNearbyEntities(disableWhenNearbyEnemyBlocks, disableWhenNearbyEnemyBlocks,
				disableWhenNearbyEnemyBlocks)) {
			if (e1 instanceof Player) {
				FPlayer entityFPlayer = FPlayers.getInstance().getByPlayer((Player) e1);
				if (entityFPlayer.getFaction().getRelationTo(fp.getFaction()) == Relation.ENEMY) {
					if (PlayerHandler.getPlayerData().get(entityFPlayer.getPlayer().getUniqueId()).isfStealth()) {
						continue;
					} else {
						nearbyEnemy = true;
						break;
					}
				}
			}
		}
		return nearbyEnemy;
	}

	@EventHandler (ignoreCancelled=true)
	public void initFlyRunnable(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		FPlayer fp = FPlayers.getInstance().getByPlayer(p);
		task = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(FAIOPlugin.getInstance(), new Runnable() {

			@Override
			public void run() {
				if (PlayerHandler.getPlayerData().get(p.getUniqueId()).isfStealth()) {
					return;
				}
				if (p.getGameMode() == GameMode.CREATIVE) {
					return;
				}
				if (StaffHandler.inStaffMode.containsKey(p.getUniqueId())) {
					return;
				}
				if (!isFlying.contains(p.getUniqueId())) {
					if (!LogHandler.inCombat.containsKey(p.getPlayer().getUniqueId())) {
						if (p.hasPermission("faio.factionsfly")) {
							if (p.hasPermission("faio.factionsfly.all") && Board.getInstance().getFactionAt(new FLocation(p)).isWilderness()) {
								if (!nearbyEnemy(p, fp)) {
									enableFlySync(p);
								}
							}
							else if (fp.isInAllyTerritory() || fp.isInOwnTerritory() && !Board.getInstance().getFactionAt(new FLocation(p)).isWilderness()) {
								if (!nearbyEnemy(p, fp)) {
									enableFlySync(p);
								}
							}
						}
					}
				}
				else {
					if (LogHandler.inCombat.containsKey(p.getPlayer().getUniqueId())) {
						disableFlySync(p);
						p.sendMessage(Utils.cc(inCombatWhileFlyingText));
					}
					else if (Board.getInstance().getFactionAt(new FLocation(p)).isWilderness() && !p.hasPermission("faio.factionsfly.all")) {
						disableFlySync(p);
						p.sendMessage(Utils.cc(leaveOwnTerritoryWhileFlyingText));
					}
					else if (!fp.isInAllyTerritory() && !fp.isInOwnTerritory() && !Board.getInstance().getFactionAt(new FLocation(p)).isWilderness()) {
						disableFlySync(p);
						p.sendMessage(Utils.cc(leaveOwnTerritoryWhileFlyingText));
					}
					else {
						if (nearbyEnemy(p, fp)) {
							disableFlySync(p);
							p.sendMessage(Utils.cc(nearbyEnemyWhileFlyingText));
						}
					}
				}
			}

		}, 0, 20);
		flyRunnables.put(e.getPlayer().getUniqueId(), task);
	}

	public void disableFlySync(Player p) {
		Bukkit.getScheduler().runTask(FAIOPlugin.getInstance(), new Runnable() {

			@Override
			public void run() {
				BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						if (cancelFallDamage.containsKey(p.getUniqueId())) {
							cancelFallDamage.get(p.getUniqueId()).cancel();
							cancelFallDamage.remove(p.getUniqueId());
						}
					}
				}, 20 * 5);
				cancelFallDamage.put(p.getUniqueId(), task);
				p.setFlying(false);
				p.setAllowFlight(false);
				isFlying.remove(p.getUniqueId());
				if (FAIOPlugin.scoreboardEnabled) {
					ScoreboardHandler.playerScoreboards.get(p.getUniqueId()).getFactionsFly()
					.setSuffix(Utils.cc(" &fDisabled"));
				}
			}
		});
	}

	public void enableFlySync(Player p) {
		Bukkit.getScheduler().runTask(FAIOPlugin.getInstance(), new Runnable() {

			@Override
			public void run() {
				if (cancelFallDamage.containsKey(p.getUniqueId())) {
					cancelFallDamage.get(p.getUniqueId()).cancel();
					cancelFallDamage.remove(p.getUniqueId());
				}
				p.setAllowFlight(true);
				p.setFlying(true);
				isFlying.add(p.getUniqueId());
				p.sendMessage(Utils.cc("&4&l(!) &eFly has been enabled"));
				if (FAIOPlugin.scoreboardEnabled) {
					ScoreboardHandler.playerScoreboards.get(p.getUniqueId()).getFactionsFly()
					.setSuffix(Utils.cc(" &fEnabled"));
				}
			}
		});
	}
	
	@EventHandler
	public void cancelFallDamage (EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getCause() == DamageCause.FALL) {
				if (cancelFallDamage.containsKey(((Player) e.getEntity()).getUniqueId())) {
					e.setCancelled(true);
				}
			}
		}
	}
}
