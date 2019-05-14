package io.flixion.crates;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.flixion.data.PlayerHandler;
import io.flixion.main.FAIOPlugin;
import io.flixion.main.ItemDataUtil;
import io.flixion.main.Utils;

public class Crates implements Listener {
	
	private String insufficientKeysText;
	private String winItemText;
	
	public Crates(String insufficientKeysText, String winItemText) {
		super();
		this.insufficientKeysText = insufficientKeysText;
		this.winItemText = winItemText;
	}
	
	public static ArrayList<CrateObject> activeCrates = new ArrayList<>();
	Random ran = new Random();

	public static ArrayList<CrateObject> getActiveCrates() {
		return activeCrates;
	}

	public static void setActiveCrates(ArrayList<CrateObject> activeCrates) {
		Crates.activeCrates = activeCrates;
	}
	
	@EventHandler (ignoreCancelled=true)
	public void joinCrateSQL (PlayerJoinEvent e) {
		if (activeCrates.size() > 0) {
			CrateSQL.insertCheckPlayer(e.getPlayer());
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void crateInteract (PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.ENDER_PORTAL_FRAME) {
				for (CrateObject c : activeCrates) {
					if (e.getClickedBlock().getLocation().equals(c.getCrateLoc())) {
						e.getPlayer().openInventory(callInitCrateInventory(c.getName(), e.getPlayer()));
						break;
					}
				}
			}
		}
	}
	
	public Inventory callInitCrateInventory(String name, Player p) {
		Inventory i = Bukkit.createInventory(null, 45 , name + " Crate");
		for (int j = 0; j < i.getSize(); j++) {
			i.setItem(j, new ItemStack(Material.STAINED_GLASS_PANE));
		}
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ItemDataUtil.encodeString(PlayerHandler.getPlayerData().get(p.getUniqueId()).getCrateKeys().get(name) + ""));
		lore.add(Utils.cc("&eYou have &4" + PlayerHandler.getPlayerData().get(p.getUniqueId()).getCrateKeys().get(name) + " &ekeys available"));
		i.setItem(20, Utils.createItem(Material.TRIPWIRE_HOOK, Utils.cc("&6&lOpen Crate"), lore, (short) 0, 1));
		i.setItem(24, Utils.createItem(Material.GOLD_INGOT, Utils.cc("&4&lView Rewards"), null, (short) 0, 1));
		return i;
	}
	
	@EventHandler (ignoreCancelled=true)
	public void cancelChangeCrateInventoryChange (InventoryClickEvent e) {
		if (e.getClickedInventory() != null && e.getWhoClicked() instanceof Player) {
			if (e.getClickedInventory().getName().contains("Crate")) {
				Player p = (Player) e.getWhoClicked();
				e.setCancelled(true);
				if (e.getCurrentItem() != null) {
					ItemStack i = e.getCurrentItem();
					if (i.getType() == Material.GOLD_INGOT) {
						if (i.hasItemMeta()) {
							if (i.getItemMeta().getDisplayName().equals(Utils.cc("&4&lView Rewards"))) {
								String crateName = e.getClickedInventory().getName().replace(" Crate", "");
								for (CrateObject c : Crates.getActiveCrates()) {
									if (c.getName().equals(crateName)) {
										e.getWhoClicked().openInventory(c.getI());
									}
								}
							}
						}
					}
					else if (i.getType() == Material.TRIPWIRE_HOOK) {
						if (i.hasItemMeta()) {
							if (i.getItemMeta().getDisplayName().equals(Utils.cc("&6&lOpen Crate"))) {
								String crateName = e.getClickedInventory().getName().replace(" Crate", "");
								if (Integer.parseInt(ItemDataUtil.extractHiddenString(i.getItemMeta().getLore().get(0))) > 0) {
									for (CrateObject c : Crates.getActiveCrates()) {
										if (c.getName().equals(crateName)) {
											Inventory open = Bukkit.createInventory(null, 45, c.getName() + " Crate");
											open.setContents(c.getDefaultContents());
											e.getWhoClicked().openInventory(open);
											initPickRewardStep1(p, open);
											initPickRewardStep2(p, open);
											initPickRewardStep3(p, open);
											initPickRewardStep4(p, open);
											initPickRewardStep5(p);
											PlayerHandler.getPlayerData().get(p.getUniqueId()).getCrateKeys().replace(crateName, PlayerHandler.getPlayerData().get(p.getUniqueId()).getCrateKeys().get(crateName) - 1);
											CrateSQL.updatePlayerKeys(p, crateName);
										}
									}
								}
								else {
									p.sendMessage(Utils.cc(insufficientKeysText));
								}
								for (CrateObject c : Crates.getActiveCrates()) {
									if (c.getName().equals(crateName)) {
										e.getWhoClicked().openInventory(c.getI());
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void initPickRewardStep1(Player p, Inventory c) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < 9; i++) {
					c.setItem(i, new ItemStack(Material.GOLD_INGOT));
				}
				for (int i = 36; i < 45; i++) {
					c.setItem(i, new ItemStack(Material.GOLD_INGOT));
				}
				p.openInventory(c);
				for (int j = 9; j < 17; j++) {
					if (c.getItem(j) != null) {
						int winPercentage = Integer.parseInt(ItemDataUtil.extractHiddenString(c.getItem(j).getItemMeta().getLore().get(0)).split("#")[0]);
						int generatedPercentage = ran.nextInt(101);
						if (generatedPercentage > winPercentage) {
							c.setItem(j, new ItemStack(Material.REDSTONE));
						}
						else {
							p.playSound(p.getLocation(), Sound.SUCCESSFUL_HIT, 10F, 10F);
							p.sendMessage(Utils.cc(winItemText).replaceAll("%itemWon%", c.getItem(j).getItemMeta().getDisplayName()).replaceAll("%crateName%", c.getName().replace(" Crate", "")));
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), ItemDataUtil.extractHiddenString(c.getItem(j).getItemMeta().getLore().get(0)).split("#")[1].replaceAll("%target%", p.getName()));
						}
					}
					else {
						c.setItem(j, new ItemStack(Material.REDSTONE));
					}
				}
				p.openInventory(c);
			}
		}, 20);
	}
	public void initPickRewardStep2(Player p, Inventory c) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < 9; i++) {
					c.setItem(i, new ItemStack(Material.DIAMOND));
				}
				for (int i = 36; i < 45; i++) {
					c.setItem(i, new ItemStack(Material.DIAMOND));
				}
				p.openInventory(c);
				for (int j = 18; j < 26; j++) {
					if (c.getItem(j) != null) {
						int winPercentage = Integer.parseInt(ItemDataUtil.extractHiddenString(c.getItem(j).getItemMeta().getLore().get(0)).split("#")[0]);
						int generatedPercentage = ran.nextInt(101);
						if (generatedPercentage > winPercentage) {
							c.setItem(j, new ItemStack(Material.REDSTONE));
						}
						else {
							p.playSound(p.getLocation(), Sound.SUCCESSFUL_HIT, 10F, 10F);
							p.sendMessage(Utils.cc(winItemText).replaceAll("%itemWon%", c.getItem(j).getItemMeta().getDisplayName()).replaceAll("%crateName%", c.getName().replace(" Crate", "")));
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), ItemDataUtil.extractHiddenString(c.getItem(j).getItemMeta().getLore().get(0)).split("#")[1].replaceAll("%target%", p.getName()));
						}
					}
					else {
						c.setItem(j, new ItemStack(Material.REDSTONE));
					}
				}
				p.openInventory(c);
			}
			
		}, 40);
	}
	public void initPickRewardStep3(Player p, Inventory c) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < 9; i++) {
					c.setItem(i, new ItemStack(Material.EMERALD));
				}
				for (int i = 36; i < 45; i++) {
					c.setItem(i, new ItemStack(Material.EMERALD));
				}
				p.openInventory(c);
				for (int j = 27; j < 35; j++) {
					if (c.getItem(j) != null) {
						int winPercentage = Integer.parseInt(ItemDataUtil.extractHiddenString(c.getItem(j).getItemMeta().getLore().get(0)).split("#")[0]);
						int generatedPercentage = ran.nextInt(101);
						if (generatedPercentage > winPercentage) {
							c.setItem(j, new ItemStack(Material.REDSTONE));
						}
						else {
							p.playSound(p.getLocation(), Sound.SUCCESSFUL_HIT, 10F, 10F);
							p.sendMessage(Utils.cc(winItemText).replaceAll("%itemWon%", c.getItem(j).getItemMeta().getDisplayName()).replaceAll("%crateName%", c.getName().replace(" Crate", "")));
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), ItemDataUtil.extractHiddenString(c.getItem(j).getItemMeta().getLore().get(0)).split("#")[1].replaceAll("%target%", p.getName()));
						}
					}
					else {
						c.setItem(j, new ItemStack(Material.REDSTONE));
					}
				}
				p.openInventory(c);
			}
			
		}, 60);
	}
	public void initPickRewardStep4(Player p, Inventory c) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(FAIOPlugin.getInstance(), new Runnable() {
			
			public void run() {
				for (int i = 0; i < 9; i++) {
					c.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE));
				}
				for (int i = 36; i < 45; i++) {
					c.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE));
				}
				p.openInventory(c);
				p.closeInventory();
			}
			
		}, 80);
	}
	public void initPickRewardStep5(Player p) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(FAIOPlugin.getInstance(), new Runnable() {
			
			public void run() {
				
				p.closeInventory();
			}
			
		}, 100);
	}
	
	@EventHandler (ignoreCancelled=true)
	public void cancelBreakCrate(BlockBreakEvent e) {
		if (e.getBlock().getType() == Material.ENDER_PORTAL_FRAME) {
			for (CrateObject c : activeCrates) {
				if (c.getCrateLoc().equals(e.getBlock().getLocation())) {
					e.setCancelled(true);
					e.getPlayer().sendMessage(Utils.cc("&eYou cannot break this block as it is a crate! Use /crate delete <crateName>"));
				}
			}
		}
	}
}
