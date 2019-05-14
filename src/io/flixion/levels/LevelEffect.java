package io.flixion.levels;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

import io.flixion.main.FAIOPlugin;
import io.flixion.staffmode.StaffHandler;

public class LevelEffect implements Listener{
	Random ran = new Random();
	private HashMap<UUID, Integer> potionEffectRunnables = new HashMap<>();
	int taskID;
	
	public boolean levelChecks(Player p, int level) {
		FPlayer fp = FPlayers.getInstance().getByPlayer(p);
		if (!fp.getFaction().isWilderness() && !fp.getFaction().isSafeZone() && !fp.getFaction().isWarZone()) {
			if (Levels.activeFactions.get(fp.getFaction().getTag()).getLevel() >= level) {
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
	public void initEffectRunnables(PlayerJoinEvent e) {
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(FAIOPlugin.getInstance(), new Runnable() {
			FPlayer p = FPlayers.getInstance().getByPlayer(e.getPlayer());
			@Override
			public void run() {
				if (StaffHandler.inStaffMode.containsKey(e.getPlayer().getUniqueId())) {
					return;
				}
				if (levelChecks(e.getPlayer(), 2)) {
					if (p.isInOwnTerritory()) {
						e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, 1));
					}
				}
				if (levelChecks(e.getPlayer(), 7)) {
					if (p.isInOwnTerritory()) {
						e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0));
						e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
					}
				}
				if (levelChecks(e.getPlayer(), 8)) {
					if (p.isInOwnTerritory()) {
						e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 0));
					}
				}
			}
		}, 0, 20);
		potionEffectRunnables.put(e.getPlayer().getUniqueId(), taskID);
	}
	
	@EventHandler (ignoreCancelled=true)
	public void endEffectRunnables(PlayerQuitEvent e) {
		if (potionEffectRunnables.containsKey(e.getPlayer().getUniqueId())) {
			Bukkit.getScheduler().cancelTask(potionEffectRunnables.get(e.getPlayer().getUniqueId()));
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void levelThreePerk(BlockBreakEvent e) {
		if (!e.isCancelled()) {
			if (e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
				if (levelChecks(e.getPlayer(), 3)) {
					if (e.getBlock().getType() == Material.STONE) {
						if (Math.random() <= 0.0001) {
							e.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
						}
						else if (Math.random() <= 0.001) {
							e.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 1));
						}
						else if (ran.nextInt(101) <= 0.005) {
							e.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1));
						}
						else if (ran.nextInt(101) <= 0.01) {
							e.getPlayer().getInventory().addItem(new ItemStack(Material.COAL, 1));
						}
					}
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void levelFourPerk (EntityDeathEvent e) {
		if (e.getEntity().getKiller() instanceof Player) {
			Player p = (Player) e.getEntity().getKiller();
			if (levelChecks(p, 4)) {
				if (ran.nextInt(101) <= 25) {
					e.setDroppedExp(e.getDroppedExp() + 5);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void levelFivePerk (EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getCause() == DamageCause.FALL) {
				if (levelChecks((Player) e.getEntity(), 5)) {
					if (ran.nextInt(101) <= 25) {
						e.setDamage(0);
						e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation(), Effect.MOBSPAWNER_FLAMES, 100);
					}
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void levelSixPerk (PlayerItemConsumeEvent e) {
		if (e.getItem().getType() == Material.POTION) {
			if (levelChecks(e.getPlayer(), 6)) {
				int durationTicks = 0;
				Potion pot = Potion.fromItemStack(e.getItem());
				for (PotionEffect pe : pot.getEffects()) {
					durationTicks = pe.getDuration();
				}
				e.getPlayer().addPotionEffect(new PotionEffect(pot.getType().getEffectType(), (int) (durationTicks + durationTicks * 0.25), pot.getLevel() - 1));
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void levelNinePerk (EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (levelChecks((Player) e.getEntity(), 9)) {
				FPlayer p = FPlayers.getInstance().getByPlayer((Player) e.getEntity());
				if (p.isInOwnTerritory()) {
					e.setDamage(e.getDamage() - e.getDamage() * 0.25);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void levelTenPerk (FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player) {
			if (levelChecks((Player) e.getEntity(), 10)) {
				e.setFoodLevel(20);
			}
		}
	}
}
