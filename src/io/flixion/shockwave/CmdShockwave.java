package io.flixion.shockwave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.flixion.main.ItemDataUtil;
import io.flixion.main.Utils;

public class CmdShockwave extends BukkitCommand {
	private String displayName;
	private ArrayList<String> lore;
	private HashMap<Enchantment, Integer> defaultEnchantments;

	public CmdShockwave(String displayName, ArrayList<String> lore, HashMap<Enchantment, Integer> defaultEnchantments, String name) {
		super(name);
		this.description = "Create shockwave tool";
		this.usageMessage = "/shockwave";
		this.displayName = displayName;
		this.lore = lore;
		this.defaultEnchantments = defaultEnchantments;
	}
	
	public boolean execute (CommandSender sender, String label, String [] args) {
		if (this.getName().equalsIgnoreCase("shockwave")) {
			if (sender.hasPermission("faio.shockwave")) {
				if (args.length == 4) {
					if (args[0].equalsIgnoreCase("give")) {//shockwave give <Player> <Type> <ItemType>
						if (Bukkit.getPlayer(args[1]) != null) {
							try {
								int size = Integer.parseInt(args[2]);
								try {
									Material m = Material.valueOf(args[3].toUpperCase());
									Player target = Bukkit.getPlayer(args[1]);
									boolean fullInv = true;
									for (ItemStack i : target.getInventory().getContents()) {
										if (i == null) {
											fullInv = false;
											break;
										}
									}
									ArrayList<String> copyLore = new ArrayList<>();
									copyLore.add(ItemDataUtil.encodeString("Shockwave#" + ((int) (size - 1)/2)));
									for (String s : lore) {
										copyLore.add(s.replaceAll("%breakRadius%", size + "x" + size).replaceAll("%toolType%", m.toString().replaceAll("_", " ").toLowerCase()));
									}
									ItemStack tool = Utils.createItem(m, Utils.cc(displayName).replaceAll("%breakRadius%", size + "x" + size).replaceAll("%toolType%", m.toString().replaceAll("_", " ").toLowerCase()), copyLore, (short) 0, 1);
									ItemMeta toolMeta = tool.getItemMeta();
									toolMeta.spigot().setUnbreakable(true);
									tool.setItemMeta(toolMeta);
									for (Map.Entry<Enchantment, Integer> e : defaultEnchantments.entrySet()) {
										tool.addUnsafeEnchantment(e.getKey(), e.getValue());
									}
									if (fullInv) {
										target.getLocation().getWorld().dropItemNaturally(target.getLocation(), tool);
									}
									else {
										target.getInventory().addItem(tool);
									}
									sender.sendMessage(Utils.cc("&4&l(!) &eYou have given a &4" + size + "x" + size + " &eShockwave tool to Player: &a" + target.getName()));
									target.sendMessage(Utils.cc("&4&l(!) &eYou have received a &4" + size + "x" + size + " &eShockwave tool"));
								} 
								catch (IllegalArgumentException e) {
									sender.sendMessage(Utils.cc("&4&l(!) Enter a valid item type"));
									shockwaveCommands(sender);
								}
							}
							catch (NumberFormatException e) {
								sender.sendMessage(Utils.cc("&4&l(!) Enter a valid radius size"));
								shockwaveCommands(sender);
							}
						}
						else {
							sender.sendMessage(Utils.cc("&4&l(!) This player is not online"));
						}
					}
					else {
						shockwaveCommands(sender);
					}
				}
				else {
					shockwaveCommands(sender);
				}
			}
		}
		return true;
	}
	
	public void shockwaveCommands(CommandSender p) {
		p.sendMessage(Utils.cc("&4------ Shockwave Commands ------"));
		p.sendMessage(Utils.cc(" "));
		p.sendMessage(Utils.cc("&e /shockwave give <playerName> <size> <itemType"));
		p.sendMessage(Utils.cc("&e playerName - Name of the player that is online"));
		p.sendMessage(Utils.cc("&e size - The radius break size, eg. 3 will result in a 3x3 pickaxe"));
		p.sendMessage(Utils.cc("&e itemType - The item enum, eg. diamond_pickaxe will result in a diamond pickaxe being given"));
	}
}
