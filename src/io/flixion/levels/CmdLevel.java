package io.flixion.levels;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

import io.flixion.main.Utils;

public class CmdLevel implements Listener{
	private String notInAFactionText;
	
	public CmdLevel(String notInAFactionText) {
		super();
		this.notInAFactionText = notInAFactionText;
	}

	@EventHandler (ignoreCancelled=true)
	public void onCommand (PlayerCommandPreprocessEvent e) {
		if (e.getMessage().equalsIgnoreCase("/f level")) {
			Player p = e.getPlayer();
			if (p.hasPermission("faio.flevel")) {
				FPlayer fp = FPlayers.getInstance().getByPlayer(p);
				if (!fp.getFaction().isWilderness() && !fp.getFaction().isSafeZone() && !fp.getFaction().isWarZone()) {
					p.openInventory(fLevelInfoInv(FPlayers.getInstance().getByPlayer(p)));
				}
				else {
					p.sendMessage(Utils.cc(notInAFactionText));
				}
			}
			e.setCancelled(true);
		}
	}
	
	public Inventory fLevelInfoInv(FPlayer p) {
		Inventory i = Bukkit.createInventory(null, 45, Utils.cc("&4&lFaction Levels"));
		ArrayList<String> lore = new ArrayList<>();
		for (int j = 0; j < 45; j++) {
			i.setItem(j, new ItemStack(Material.STAINED_GLASS_PANE));
		}
		lore.add(Utils.cc("&eFaction: &4" + p.getFaction().getTag()));
		lore.add(Utils.cc("&eCurrent EXP: &4" + Levels.activeFactions.get(p.getFaction().getTag()).getEXP()));
		i.setItem(12, Utils.createItem(Material.EXP_BOTTLE, "&aEXP Information", lore, (short) 0, 1));
		lore.clear();
		lore.add(Utils.cc("&eCurrent Level: &a" + Levels.activeFactions.get(p.getFaction().getTag()).getLevel()));
		if (Levels.activeFactions.get(p.getFaction().getTag()).getLevel() == 10) {
			lore.add(Utils.cc("&eEXP to next level: &4You are the maximum level"));
		}
		else {
			lore.add(Utils.cc("&eEXP to next level: &4" + (Levels.getUpgradeCosts().get(Levels.activeFactions.get(p.getFaction().getTag()).getLevel() + 1) - Levels.activeFactions.get(p.getFaction().getTag()).getEXP())));
		}
		i.setItem(14, Utils.createItem(Material.BLAZE_POWDER, "&aLevel Information", lore, (short) 0, 1));
		int level = Levels.activeFactions.get(p.getFaction().getTag()).getLevel();
		lore.clear();
		
		lore.add(" ");
		lore.add("  ");
		lore.add("   ");
		//Level 2
		lore.set(1, Utils.cc("&6Permanent Haste II in own claims"));
		if (level >= 2) {
			lore.set(2, Utils.cc("&2&lUnlocked"));
		}
		else {
			lore.set(2, Utils.cc("&c&lLocked (Unlock at Level 2)"));
		}
		i.setItem(27, Utils.createItem(Material.DIAMOND_PICKAXE, Utils.cc("&7&lMining Speed"), lore, (short) 0, 1));
		//Level 3
		lore.set(1, Utils.cc("&6Increased rare drops when mining"));
		if (level >= 3) {
			lore.set(2, Utils.cc("&2&lUnlocked"));
		}
		else {
			lore.set(2, Utils.cc("&c&lLocked (Unlock at Level 3)"));
		}
		i.setItem(28, Utils.createItem(Material.DIAMOND, Utils.cc("&7&lMining Fortune"), lore, (short) 0, 1));
		//Level 4
		lore.set(1, Utils.cc("&6Increased EXP when killing mobs"));
		if (level >= 4) {
			lore.set(2, Utils.cc("&2&lUnlocked"));
		}
		else {
			lore.set(2, Utils.cc("&c&lLocked (Unlock at Level 4)"));
		}
		i.setItem(29, Utils.createItem(Material.EXP_BOTTLE, Utils.cc("&7&lEXP Fortune"), lore, (short) 0, 1));
		//Level 5
		lore.set(1, Utils.cc("&625% chance to cancel fall damage"));
		if (level >= 5) {
			lore.set(2, Utils.cc("&2&lUnlocked"));
		}
		else {
			lore.set(2, Utils.cc("&c&lLocked (Unlock at Level 5)"));
		}
		i.setItem(30, Utils.createItem(Material.FEATHER, Utils.cc("&7&lFeather Falling"), lore, (short) 0, 1));
		//Level 6
		lore.set(1, Utils.cc("&625% longer potion effects"));
		if (level >= 6) {
			lore.set(2, Utils.cc("&2&lUnlocked"));
		}
		else {
			lore.set(2, Utils.cc("&c&lLocked (Unlock at Level 6)"));
		}
		i.setItem(31, Utils.createItem(Material.POTION, Utils.cc("&7&lAlchemical Boost"), lore, (short) 0, 1));
		//Level 7
		lore.set(1, Utils.cc("&6Speed & Strength I in own claims"));
		if (level >= 7) {
			lore.set(2, Utils.cc("&2&lUnlocked"));
		}
		else {
			lore.set(2, Utils.cc("&c&lLocked (Unlock at Level 7)"));
		}
		i.setItem(32, Utils.createItem(Material.DIAMOND_SWORD, Utils.cc("&7&lPvP Effects"), lore, (short) 0, 1));
		//Level 8
		lore.set(1, Utils.cc("&6Regeneration I in own claims"));
		if (level >= 8) {
			lore.set(2, Utils.cc("&2&lUnlocked"));
		}
		else {
			lore.set(2, Utils.cc("&c&lLocked (Unlock at Level 8)"));
		}
		i.setItem(33, Utils.createItem(Material.GHAST_TEAR, Utils.cc("&7&lHealing"), lore, (short) 0, 1));
		//Level 9
		lore.set(1, Utils.cc("&625% less damage in own claims"));
		if (level >= 9) {
			lore.set(2, Utils.cc("&2&lUnlocked"));
		}
		else {
			lore.set(2, Utils.cc("&c&lLocked (Unlock at Level 9)"));
		}
		i.setItem(34, Utils.createItem(Material.DIAMOND_HELMET, Utils.cc("&7&lProtection"), lore, (short) 0, 1));
		//Level 10
		lore.set(1, Utils.cc("&6No hunger loss, globally"));
		if (level == 10) {
			lore.set(2, Utils.cc("&2&lUnlocked"));
		}
		else {
			lore.set(2, Utils.cc("&c&lLocked (Unlock at Level 10)"));
		}
		i.setItem(35, Utils.createItem(Material.COOKED_BEEF, Utils.cc("&7&lSated Appetite"), lore, (short) 0, 1));
		return i;
	}
}
