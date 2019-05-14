package io.flixion.crates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import io.flixion.data.PlayerHandler;
import io.flixion.data.PlayerProfile;
import io.flixion.main.FAIOPlugin;
import io.flixion.main.ItemDataUtil;
import io.flixion.main.Utils;

public class CrateObject {
	private String name;
	private ArmorStand hologram;
	private Location crateLoc;
	private int taskID;
	private Effect effect;
	private Inventory i;
	private ItemStack[] defaultContents;
	
	@SuppressWarnings("deprecation")
	public void createCrate(String crateName, Player p, String titleFormatText) {
		Location crateLoc = new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY() - 1, p.getLocation().getBlockZ());
		FAIOPlugin.getCratesConfig().set("crates." + crateName + ".particleEffect", Effect.MOBSPAWNER_FLAMES.toString());
		FAIOPlugin.getCratesConfig().set("crates." + crateName + ".location", crateLoc.getBlockX() + "," + crateLoc.getBlockY() + "," + crateLoc.getBlockZ() + "," + crateLoc.getWorld().getName());
		for (int i = 0; i < 27; i++) {
			List<String> lore = new ArrayList<String>();
			lore.add("Line 1");
			lore.add("Line 2");
			FAIOPlugin.getCratesConfig().set("crates." + crateName + ".contents." + i + ".itemName", "&aExample Item");
			FAIOPlugin.getCratesConfig().set("crates." + crateName + ".contents." + i + ".itemID", 2);
			FAIOPlugin.getCratesConfig().set("crates." + crateName + ".contents." + i + ".chanceToWinPercent", 50);
			FAIOPlugin.getCratesConfig().set("crates." + crateName + ".contents." + i + ".commandToExecuteOnWin", "give %target% obsidian 64");
			FAIOPlugin.getCratesConfig().set("crates." + crateName + ".contents." + i + ".itemLore", lore);
			FAIOPlugin.saveCratesFile();
		}
		FAIOPlugin.saveCratesFile();
		ArmorStand as = (ArmorStand) p.getWorld().spawnEntity(new Location(p.getWorld(), p.getLocation().getBlockX() + 0.500, p.getLocation().getBlockY() - 1, p.getLocation().getBlockZ() + 0.500), EntityType.ARMOR_STAND);
		as.setGravity(false);
		as.setSmall(true);
		as.setCanPickupItems(false);
		as.setCustomName("  " + Utils.cc(titleFormatText.replace("%name%", crateName)));
		as.setCustomNameVisible(true);
		as.setVisible(false);
		this.hologram = as;
		p.getWorld().getBlockAt(new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY() - 3, p.getLocation().getBlockZ())).setType(Material.FENCE);
		p.getWorld().getBlockAt(new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY() - 2, p.getLocation().getBlockZ())).setType(Material.FENCE);
		p.getWorld().getBlockAt(new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY() - 1, p.getLocation().getBlockZ())).setType(Material.ENDER_PORTAL_FRAME);
		int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				p.getWorld().playEffect(crateLoc, Effect.MOBSPAWNER_FLAMES, 10, 20);
			}
		}, 0, 10);
		
		this.name = crateName;
		this.crateLoc = crateLoc;
		this.effect = Effect.MOBSPAWNER_FLAMES;
		this.taskID = taskID;
		this.i = Bukkit.getServer().createInventory(null, 45, name + " Crate");
		for (int j = 0; j < 9; j++) {
			this.i.setItem(j, new ItemStack(Material.STAINED_GLASS_PANE));
		}
		for (int j = 36; j < 45; j++) {
			this.i.setItem(j, new ItemStack(Material.STAINED_GLASS_PANE));
		}
		int index = 9;
		for (String c : FAIOPlugin.getCratesConfig().getConfigurationSection("crates." + name + ".contents").getKeys(false)) {
			ItemStack i = new ItemStack (Material.getMaterial(FAIOPlugin.getCratesConfig().getInt("crates." + name + ".contents." + c + ".itemID")));
			ItemMeta m = i.getItemMeta();
			ArrayList<String> iLore = new ArrayList<String>();
			iLore.add(ItemDataUtil.encodeString(FAIOPlugin.getCratesConfig().getString("crates." + name + ".contents." + c + ".chanceToWinPercent") + "#" + FAIOPlugin.getCratesConfig().getString("crates." + name + ".contents." + c + ".commandToExecuteOnWin")));
			for (String s : FAIOPlugin.getCratesConfig().getStringList("crates." + name + ".contents." + c + ".itemLore")) {
				iLore.add(Utils.cc(s));
			}
			m.setDisplayName(Utils.cc(FAIOPlugin.getCratesConfig().getString("crates." + name + ".contents." + c + ".itemName")));
			m.setLore(iLore);
			i.setItemMeta(m);
			this.i.setItem(index, i);
			index++;
		}
		defaultContents = i.getContents();
		for (Map.Entry<UUID, PlayerProfile> entry : PlayerHandler.getPlayerData().entrySet()) {
			entry.getValue().getCrateKeys().put(crateName, 0);
		}
		FAIOPlugin.crateNames.add(crateName);
		CrateSQL.appendCratesTbl(name);
	}

	public ItemStack[] getDefaultContents() {
		return defaultContents;
	}

	public void setDefaultContents(ItemStack[] defaultContents) {
		this.defaultContents = defaultContents;
	}

	public String getName() {
		return name;
	}

	public ArmorStand getHologram() {
		return hologram;
	}

	public Location getCrateLoc() {
		return crateLoc;
	}

	public int getTaskID() {
		return taskID;
	}

	public Effect getEffect() {
		return effect;
	}

	public Inventory getI() {
		return i;
	}

	public void setI(Inventory i) {
		this.i = i;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHologram(ArmorStand hologram) {
		this.hologram = hologram;
	}

	public void setCrateLoc(Location crateLoc) {
		this.crateLoc = crateLoc;
	}

	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	public void setEffect(Effect effect) {
		this.effect = effect;
	}
}
