package io.flixion.staffmode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import io.flixion.combatlog.LogHandler;
import io.flixion.factionsfly.Fly;
import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;
import io.flixion.scoreboard.ScoreboardHandler;

public class CmdStaffMode extends BukkitCommand {
	private Material freezeItem;
	private Material randomTeleportItem;
	private Material knockbackItem;
	private Material cpsCheckerItem;
	private Material leaveStaffModeItem;
	private ArrayList<String> freezeItemLore;
	private ArrayList<String> randomTeleportItemLore;
	private ArrayList<String> knockbackItemLore;
	private ArrayList<String> cpsCheckerItemLore;
	private ArrayList<String>  leaveStaffModeLore;
	private String freezeItemName;
	private String randomTeleportItemName;
	private String knockbackItemName;
	private String cpsCheckerItemName;
	private String  leaveStaffModeName;
	private String enabledStaffText;
	private static String disabledStaffText;
	private String staffEnteredStaffModeNotification;
	private static String staffLeftStaffModeNotification;
	

	public CmdStaffMode(Material freezeItem, Material randomTeleportItem, Material knockbackItem,
			Material cpsCheckerItem, Material leaveStaffModeItem, ArrayList<String> freezeItemLore,
			ArrayList<String> randomTeleportItemLore, ArrayList<String> knockbackItemLore,
			ArrayList<String> cpsCheckerItemLore, ArrayList<String> leaveStaffModeLore, String freezeItemName,
			String randomTeleportItemName, String knockbackItemName, String cpsCheckerItemName,
			String leaveStaffModeName, String enabledStaffText, String disabledStaffText,
			String staffEnteredStaffModeNotification, String staffLeftStaffModeNotification, String name) {
		super(name);
		this.description = "Enter staffmode";
		List<String> aliases = new ArrayList<String>();
		aliases.add("sm");
		aliases.add("staffmode");
		this.setAliases(aliases);
		this.usageMessage = "/staff";
		this.freezeItem = freezeItem;
		this.randomTeleportItem = randomTeleportItem;
		this.knockbackItem = knockbackItem;
		this.cpsCheckerItem = cpsCheckerItem;
		this.leaveStaffModeItem = leaveStaffModeItem;
		this.freezeItemLore = freezeItemLore;
		this.randomTeleportItemLore = randomTeleportItemLore;
		this.knockbackItemLore = knockbackItemLore;
		this.cpsCheckerItemLore = cpsCheckerItemLore;
		this.leaveStaffModeLore = leaveStaffModeLore;
		this.freezeItemName = freezeItemName;
		this.randomTeleportItemName = randomTeleportItemName;
		this.knockbackItemName = knockbackItemName;
		this.cpsCheckerItemName = cpsCheckerItemName;
		this.leaveStaffModeName = leaveStaffModeName;
		this.enabledStaffText = enabledStaffText;
		CmdStaffMode.disabledStaffText = disabledStaffText;
		this.staffEnteredStaffModeNotification = staffEnteredStaffModeNotification;
		CmdStaffMode.staffLeftStaffModeNotification = staffLeftStaffModeNotification;
	}
	
	public void enableStaffMode(Player p) {
		if (LogHandler.inCombat.containsKey(p.getUniqueId())) {
			LogHandler.inCombat.get(p.getUniqueId()).cancel();
			LogHandler.inCombat.remove(p.getUniqueId());
			if (FAIOPlugin.scoreboardEnabled) {
				ScoreboardHandler.playerScoreboards.get(p.getUniqueId()).removeCombatTimerScoreboard();
			}
		}
		ArrayList<ItemStack []> playerItems = new ArrayList<>();
		playerItems.add(p.getInventory().getContents());
		playerItems.add(p.getInventory().getArmorContents());
		StaffHandler.inStaffMode.put(p.getUniqueId(), playerItems);
		if (Fly.isFlying.contains(p.getUniqueId())) {
			Fly.isFlying.remove(p.getUniqueId());
		}
		p.getInventory().clear();
		for (PotionEffect e : p.getActivePotionEffects()) {
			p.removePotionEffect(e.getType());
		}
		p.getInventory().setArmorContents(null);
		p.getInventory().setItem(0, Utils.createItem(freezeItem, Utils.cc(freezeItemName), freezeItemLore, (short) 0, 1));
		p.getInventory().setItem(2, Utils.createItem(knockbackItem, Utils.cc(knockbackItemName), knockbackItemLore, (short) 0, 1));
		p.getInventory().setItem(4, Utils.createItem(randomTeleportItem, Utils.cc(randomTeleportItemName), randomTeleportItemLore, (short) 0, 1));
		p.getInventory().setItem(6, Utils.createItem(cpsCheckerItem, Utils.cc(cpsCheckerItemName), cpsCheckerItemLore, (short) 0, 1));
		p.getInventory().setItem(8, Utils.createItem(leaveStaffModeItem, Utils.cc(leaveStaffModeName), leaveStaffModeLore, (short) 0, 1));
		p.getInventory().getItem(2).addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
		for (Player onlineP : Bukkit.getOnlinePlayers()) {
			FAIOPlugin.getEntityHider().hideEntity(onlineP, p);
		}
		p.sendMessage(Utils.cc(enabledStaffText));
		for (Map.Entry<UUID, ArrayList<ItemStack[]>> entry : StaffHandler.inStaffMode.entrySet()) {
			Bukkit.getPlayer(entry.getKey()).sendMessage(Utils.cc(staffEnteredStaffModeNotification).replaceAll("%staffPlayer%", p.getName()));
		}
		p.setGameMode(GameMode.SURVIVAL);
		p.setAllowFlight(true);
		p.setFlying(true);
		p.updateInventory();
	}
	
	public static void disableStaffMode(Player p) {
		p.getInventory().clear();
		p.getInventory().setContents(StaffHandler.inStaffMode.get(p.getUniqueId()).get(0));
		p.getInventory().setArmorContents(StaffHandler.inStaffMode.get(p.getUniqueId()).get(1));
		StaffHandler.inStaffMode.remove(p.getUniqueId());
		p.setAllowFlight(false);
		p.setFlying(false);
		p.setGameMode(GameMode.SURVIVAL);
		for (Player onlineP : Bukkit.getOnlinePlayers()) {
			FAIOPlugin.getEntityHider().showEntity(onlineP, p);
		}
		p.sendMessage(Utils.cc(disabledStaffText));
		for (Map.Entry<UUID, ArrayList<ItemStack[]>> entry : StaffHandler.inStaffMode.entrySet()) {
			Bukkit.getPlayer(entry.getKey()).sendMessage(Utils.cc(staffLeftStaffModeNotification).replaceAll("%staffPlayer%", p.getName()));
		}
		p.updateInventory();
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (p.hasPermission("faio.staffmode.use")){
				if (StaffHandler.inStaffMode.containsKey(p.getUniqueId())) {
					disableStaffMode(p);
				}
				else {
					enableStaffMode(p);
				}
			}
		}
		return true;
	}
}
