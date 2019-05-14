package io.flixion.genbucket;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.ItemDataUtil;
import io.flixion.main.Utils;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class GenBucket extends BukkitCommand implements Listener {
	private static int blocksPerSecond;
	private ArrayList<String> genBucketLore;
	private String genBucketName;
	private int genBucketItemID;
	private double cobbleCost;
	private double obsidianCost;
	private double sandCost;
	private DecimalFormat formatter = new DecimalFormat("#,###.00");
	public GenBucket(int blocksPerSecond, ArrayList<String> genBucketLore, String genBucketName, int genBucketItemID, double cobbleCost,
			double obsidianCost, double sandCost, String name) {
		super(name);
		this.description = "Access the genbucket sell shop";
		this.usageMessage = "/genbucket";
		GenBucket.blocksPerSecond = blocksPerSecond;
		this.genBucketLore = genBucketLore;
		this.genBucketName = genBucketName;
		this.genBucketItemID = genBucketItemID;
		this.cobbleCost = cobbleCost;
		this.obsidianCost = obsidianCost;
		this.sandCost = sandCost;
	}

	@SuppressWarnings("deprecation")
	public ItemStack createGenBucket (String type, String direction, int amount) {
		ArrayList<String> lore = new ArrayList<String>();
		ItemStack i = new ItemStack(Material.getMaterial(genBucketItemID), amount);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(Utils.cc(genBucketName).replace("%direction%", direction).replace("%material%", type));
		lore.add(ItemDataUtil.encodeString("GenBucket/" + type + "/" + direction));
		for (String e : genBucketLore) {
			lore.add(Utils.cc(e).replace("%direction%", direction).replace("%material%", type));
		}
		m.setLore(lore);
		i.setItemMeta(m);
		return i;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler (ignoreCancelled=true)
	public void placeBucket (PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
			if (e.getPlayer().getItemInHand() != null) {
				if (e.getPlayer().getItemInHand().getType() == Material.getMaterial(genBucketItemID)) {
					ItemStack i = e.getPlayer().getItemInHand();
					if (i.hasItemMeta()) {
						if (i.getItemMeta().hasLore()) {
							if (ItemDataUtil.hasHiddenString(i.getItemMeta().getLore().get(0))) {
								if (ItemDataUtil.extractHiddenString(i.getItemMeta().getLore().get(0)).split("/")[0].equals("GenBucket")) {
									e.setCancelled(true);
									String type = ItemDataUtil.extractHiddenString(i.getItemMeta().getLore().get(0)).split("/")[1];
									String direction = ItemDataUtil.extractHiddenString(i.getItemMeta().getLore().get(0)).split("/")[2];
									FPlayer p = FPlayers.getInstance().getByPlayer(e.getPlayer());
									if (Board.getInstance().getFactionAt(new FLocation(e.getClickedBlock())).getTag().equals(p.getFaction().getTag())) {
										Material m = Material.AIR;
										if (type.equals("Cobblestone")) {
											m = Material.COBBLESTONE;
										}
										else if (type.equals("Sand")) {
											m = Material.SAND;
										}
										else if (type.equals("Obsidian")){
											m = Material.OBSIDIAN;
										}
										if (direction.equals("Vertical")) {
											new GenBucketObject().initVerticalGenProcess(e.getClickedBlock().getLocation(), m);
											e.getPlayer().getInventory().setItemInHand(createGenBucket(type, direction, i.getAmount() - 1));
											e.getPlayer().updateInventory();
										}
										else if (direction.equals("Horizontal") && e.getBlockFace() != BlockFace.UP && e.getBlockFace() != BlockFace.DOWN) {
											new GenBucketObject().initHorizontalGenProcess(e.getClickedBlock().getLocation(), m, e.getBlockFace(), p);
											e.getPlayer().getInventory().setItemInHand(createGenBucket(type, direction, i.getAmount() - 1));
											e.getPlayer().updateInventory();
										}
										else {
											p.sendMessage(Utils.cc("&4&l(!) &eYou can only place Horizontal GenBuckets on the side of blocks!"));
										}
									}
									else {
										p.sendMessage(Utils.cc("&4&l(!) &eYou can only place GenBuckets in your own territory!"));
									}
								}
							}
						}
					}
				}
			}
		}
	}


	public static int getBlocksPerSecond() {
		return blocksPerSecond;
	}


	@Override
	public boolean execute (CommandSender sender, String label, String[] args) {
		Player p = (Player) sender;
		p.openInventory(createGenbucketShop());
		return true;
	}
	
	public Inventory createGenbucketShop (){
		Inventory i = Bukkit.createInventory(null, 27, "Genbucket Shop");
		for (int j = 0; j < i.getSize(); j++) {
			i.setItem(j, new ItemStack(Material.STAINED_GLASS_PANE));
		}
		ArrayList<String> lore = new ArrayList<String>();
		ItemStack vertObsidian = createGenBucket("Obsidian", "Vertical", 1);
		ItemStack vertSand = createGenBucket("Sand", "Vertical", 1);
		ItemStack vertCobble = createGenBucket("Cobblestone", "Vertical", 1);
		ItemStack horiObsidian = createGenBucket("Obsidian", "Horizontal", 1);
		ItemStack horiSand = createGenBucket("Sand", "Horizontal", 1);
		ItemStack horiCobble = createGenBucket("Cobblestone", "Horizontal", 1);
		ItemMeta vObbyMeta = vertObsidian.getItemMeta();
		ItemMeta vSandMeta = vertSand.getItemMeta();
		ItemMeta vCobbleMeta = vertCobble.getItemMeta();
		ItemMeta hObbyMeta = horiObsidian.getItemMeta();
		ItemMeta hSandMeta = horiSand.getItemMeta();
		ItemMeta hCobbleMeta = horiCobble.getItemMeta();
		lore = (ArrayList<String>) vObbyMeta.getLore();
		lore.add("");
		lore.set(0, ItemDataUtil.encodeString("Obsidian/Vertical/" + obsidianCost));
		lore.add(Utils.cc("&6&lBuy &b$" + formatter.format(obsidianCost)));
		vObbyMeta.setLore(lore);
		lore.clear();
		vertObsidian.setItemMeta(vObbyMeta);
		lore = (ArrayList<String>) vSandMeta.getLore();
		lore.set(0, ItemDataUtil.encodeString("Sand/Vertical/" + sandCost));
		lore.add(Utils.cc("&6&lBuy &b$" + formatter.format(sandCost)));
		vSandMeta.setLore(lore);
		lore.clear();
		vertSand.setItemMeta(vSandMeta);
		lore = (ArrayList<String>) vCobbleMeta.getLore();
		lore.set(0, ItemDataUtil.encodeString("Cobblestone/Vertical/" + cobbleCost));
		lore.add(Utils.cc("&6&lBuy &b$" + formatter.format(cobbleCost)));
		vCobbleMeta.setLore(lore);
		lore.clear();
		vertCobble.setItemMeta(vCobbleMeta);
		lore = (ArrayList<String>) hObbyMeta.getLore();
		lore.set(0, ItemDataUtil.encodeString("Obsidian/Horizontal/" + obsidianCost));
		lore.add(Utils.cc("&6&lBuy &b$" + formatter.format(obsidianCost)));
		hObbyMeta.setLore(lore);
		lore.clear();
		horiObsidian.setItemMeta(hObbyMeta);
		lore = (ArrayList<String>) hSandMeta.getLore();
		lore.set(0, ItemDataUtil.encodeString("Sand/Horizontal/" + sandCost));
		lore.add(Utils.cc("&6&lBuy &b$" + formatter.format(sandCost)));
		hSandMeta.setLore(lore);
		lore.clear();
		horiSand.setItemMeta(hSandMeta);
		lore = (ArrayList<String>) hCobbleMeta.getLore();
		lore.set(0, ItemDataUtil.encodeString("Cobblestone/Horizontal/" + cobbleCost));
		lore.add(Utils.cc("&6&lBuy &b$" + formatter.format(cobbleCost)));
		hCobbleMeta.setLore(lore);
		lore.clear();
		horiCobble.setItemMeta(hCobbleMeta);
		i.setItem(10, vertObsidian);
		i.setItem(11, vertSand);
		i.setItem(12, vertCobble);
		i.setItem(14, horiObsidian);
		i.setItem(15, horiSand);
		i.setItem(16, horiCobble);
		return i;
	}
	
	public Inventory createBulkBuyInventory(ItemStack item) {
		Inventory i = Bukkit.createInventory(null, 27, "Genbucket Bulk Shop");
		for (int j = 0; j < i.getSize(); j++) {
			i.setItem(j, new ItemStack(Material.STAINED_GLASS_PANE));
		}
		i.setItem(10, item);
		ArrayList<String> lore = new ArrayList<>();
		lore.add(Utils.cc("&6&lCost: &b$" + formatter.format((Double.parseDouble(ItemDataUtil.extractHiddenString(item.getItemMeta().getLore().get(0)).split("/")[2])))));
		i.setItem(12, Utils.createItem(Material.GOLD_INGOT, Utils.cc("&4&lPurchase 1x"), lore, (short) 0, 1));
		lore.set(0, Utils.cc("&6&lCost: &b$" + formatter.format((Double.parseDouble(ItemDataUtil.extractHiddenString(item.getItemMeta().getLore().get(0)).split("/")[2]) * 16))));
		i.setItem(13, Utils.createItem(Material.GOLD_INGOT, Utils.cc("&4&lPurchase 16x"), lore, (short) 0, 16));
		lore.set(0, Utils.cc("&6&lCost: &b$" + formatter.format((Double.parseDouble(ItemDataUtil.extractHiddenString(item.getItemMeta().getLore().get(0)).split("/")[2]) * 64))));
		i.setItem(14, Utils.createItem(Material.GOLD_INGOT, Utils.cc("&4&lPurchase 64x"), lore, (short) 0, 64));
		return i;
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler (ignoreCancelled=true)
	public void useShop (InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			if (e.getClickedInventory() != null) {
				if (e.getClickedInventory().getName().equals("Genbucket Shop")) {
					Player p = (Player) e.getWhoClicked();
					if (e.getCurrentItem() != null) {
						if (e.getCurrentItem().getType() == Material.getMaterial(genBucketItemID)) {
							p.openInventory(createBulkBuyInventory(e.getCurrentItem()));
						}
					}
					e.setCancelled(true);
				}
				else if (e.getClickedInventory().getName().equals("Genbucket Bulk Shop")) {
					Player p = (Player) e.getWhoClicked();
					if (e.getCurrentItem() != null) {
						if (e.getCurrentItem().getType() == Material.GOLD_INGOT) {
							EconomyResponse r = FAIOPlugin.getEco().withdrawPlayer(p.getName(), Double.parseDouble(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(0)).split(" ")[1].replace("$", "").replaceAll(",", "")));
							if (r.transactionSuccess()) {
								String [] bucketInfo = ItemDataUtil.extractHiddenString(e.getInventory().getItem(10).getItemMeta().getLore().get(0)).split("/");
								p.getInventory().addItem(createGenBucket(bucketInfo[0], bucketInfo[1], e.getCurrentItem().getAmount()));
								p.sendMessage(Utils.cc("&4&l(!) You have purchased x" + e.getCurrentItem().getAmount() + " genbuckets for &b$" + r.amount));
							}
							else {
								p.sendMessage(Utils.cc("&4&l(!) You have insufficient funds to purchase this"));
							}
						}
					}
					e.setCancelled(true);
				}
			}
		}
	}
}
