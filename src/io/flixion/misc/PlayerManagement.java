package io.flixion.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.earth2me.essentials.User;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FactionRelationEvent;
import com.massivecraft.factions.struct.Relation;

import de.dustplanet.util.SilkUtil;
import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;

public class PlayerManagement implements Listener{
	private boolean logoutEnabled;
	public static Map<UUID, Map<String, BukkitTask>> homeTaskList = new HashMap<>();
	private int homeLifeSeconds;
	private SilkUtil silkUtil = SilkUtil.hookIntoSilkSpanwers();
	
	public PlayerManagement(boolean logoutEnabled, int homeLifeSeconds) {
		super();
		this.logoutEnabled = logoutEnabled;
		this.homeLifeSeconds = homeLifeSeconds;
		this.homeLifeSeconds *= 20;
	}
	
	@EventHandler
	public void handleRelationChange(FactionRelationEvent e) {
		if (e.getRelation() == Relation.ENEMY) {
			for (FPlayer fp : e.getFaction().getFPlayers()) {
				User u = FAIOPlugin.getEssentials().getUser(fp.getPlayer().getUniqueId());
				for (String s : u.getHomes()) {
					try {
						if (Board.getInstance().getFactionAt(new FLocation(u.getHome(s))).getTag().equals(e.getTargetFaction().getTag())) {
							if (homeTaskList.containsKey(fp.getPlayer().getUniqueId())) {
								if (!homeTaskList.get(fp.getPlayer().getUniqueId()).containsKey(s)) {
									initLaterRemovalTask(s, fp.getPlayer().getUniqueId());
								}
							} else {
								homeTaskList.put(fp.getPlayer().getUniqueId(), new HashMap<>());
								initLaterRemovalTask(s, fp.getPlayer().getUniqueId());
							}
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void giveSpawnerWithSilk(BlockBreakEvent e) {
		if (e.isCancelled()) {
			if (e.getBlock().getType() == Material.MOB_SPAWNER) {
				if (!Board.getInstance().getFactionAt(new FLocation(e.getBlock())).isWilderness() && !Board.getInstance().getFactionAt(new FLocation(e.getBlock())).isSafeZone() && !Board.getInstance().getFactionAt(new FLocation(e.getBlock())).isWarZone()) {
					Faction f = Board.getInstance().getFactionAt(new FLocation(e.getBlock()));
					if (f.getTag() != FPlayers.getInstance().getByPlayer(e.getPlayer()).getFaction().getTag()) {
						if (e.getPlayer().getItemInHand() != null) {
							if (e.getPlayer().getItemInHand().hasItemMeta()) {
								if (e.getPlayer().getItemInHand().getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
									ItemStack spawner = silkUtil.newSpawnerItem(silkUtil.getSpawnerEntityID(e.getBlock()), silkUtil.getCustomSpawnerName(silkUtil.getCreatureName(silkUtil.getSpawnerEntityID(e.getBlock()))));
									e.getBlock().setType(Material.AIR);
									e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), spawner);
								}
							}
						}
					}
				}
			}
		}
	}
	
//	@EventHandler
//	public void preventStacking(SilkSpawnersSpawnerBreakEvent e) {
//		Bukkit.broadcastMessage("Called");
//		e.setDrop(getSingleStack(e.getDrop()));
//	}
//	
//	private ItemStack getSingleStack(ItemStack i) {
//		net.minecraft.server.v1_8_R3.ItemStack nmsIS = CraftItemStack.asNMSCopy(i);
//        nmsIS.getItem().c(1);
//        return CraftItemStack.asBukkitCopy(nmsIS);
//    }

	@EventHandler (ignoreCancelled=true)
	public void logoutInClaimedLand (PlayerQuitEvent e) {
		if (!logoutEnabled) {
			if (Board.getInstance().getFactionAt(new FLocation(e.getPlayer())).isNormal()) {
				Faction f = Board.getInstance().getFactionAt(new FLocation(e.getPlayer()));
				if (!f.getTag().equals(FPlayers.getInstance().getByPlayer(e.getPlayer()).getFaction().getTag()) && !Board.getInstance().getFactionAt(new FLocation(e.getPlayer())).isWilderness() && !(f.getRelationTo(FPlayers.getInstance().getByPlayer(e.getPlayer()).getFaction()) == Relation.ALLY)) {
					e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
				}
			}
		}
	}
	
	@EventHandler
	public void handleOldHomes(PlayerJoinEvent e) {
		User u = FAIOPlugin.getEssentials().getUser(e.getPlayer().getUniqueId());
		for (String s : u.getHomes()) {
			try {
				if (Board.getInstance().getFactionAt(new FLocation(u.getHome(s))).isNormal()) {
					Faction f = Board.getInstance().getFactionAt(new FLocation(u.getHome(s)));
					if (f.getRelationTo(FPlayers.getInstance().getByPlayer(e.getPlayer()).getFaction()).isEnemy()) {
						if (homeTaskList.containsKey(e.getPlayer().getUniqueId())) {
							if (!homeTaskList.get(e.getPlayer().getUniqueId()).containsKey(s)) {
								initLaterRemovalTask(s, e.getPlayer().getUniqueId());
							}
						} else {
							homeTaskList.put(e.getPlayer().getUniqueId(), new HashMap<>());
							initLaterRemovalTask(s, e.getPlayer().getUniqueId());
						}
					}
				}
			} catch (Exception e1) { // u.getHome(String) throws Exception??
				continue;
			}
		}
	}
	
	@EventHandler
	public void handleHomes (PlayerCommandPreprocessEvent e) {
		if (e.getMessage().startsWith("/sethome") || e.getMessage().startsWith("/esethome")) {
			if (e.getMessage().trim().split(" ").length == 2) {
				if (Board.getInstance().getFactionAt(new FLocation(e.getPlayer().getLocation())).isNormal()) {
					Faction f = Board.getInstance().getFactionAt(new FLocation(e.getPlayer().getLocation()));
					if (f.getRelationTo(FPlayers.getInstance().getByPlayer(e.getPlayer()).getFaction()).isEnemy()) {
						initLaterRemovalTask(e.getMessage().trim().split(" ")[1], e.getPlayer().getUniqueId());
					}
				}
			}
		}
	}
	
	private void initLaterRemovalTask(String homeString, UUID user) {
		BukkitTask t = Bukkit.getScheduler().runTaskLaterAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				try {
					FAIOPlugin.getEssentials().getUser(user).delHome(homeString);
					if (Bukkit.getPlayer(user) != null) {
						Bukkit.getPlayer(user).sendMessage(Utils.cc("&4&l(!) &eYour home &a'" + homeString + "' &ehas been removed from enemy territory!"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, homeLifeSeconds);
		homeTaskList.get(user).put(homeString, t);
	}
}
