package io.flixion.chunkbuster;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.flixion.main.ItemDataUtil;
import io.flixion.main.Utils;

public class CmdChunkBuster extends BukkitCommand {
	private String busterName;
	private ArrayList<String> busterLore;
	public static Material busterItemType;
	private String busterTargetCommandMessage;
	private String busterSenderCommandMessage;

	public CmdChunkBuster(String name, String busterName, ArrayList<String> busterLore, Material busterItemType,
			String busterTargetCommandMessage, String busterSenderCommandMessage) {
		super(name);
		this.busterName = busterName;
		this.busterLore = busterLore;
		CmdChunkBuster.busterItemType = busterItemType;
		this.busterTargetCommandMessage = busterTargetCommandMessage;
		this.busterSenderCommandMessage = busterSenderCommandMessage;
		usageMessage = "/chunkbuster give <Player> <Amount>";
		description = "Give a player chunkbuster(s)";
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender.hasPermission("faio.chunkbuster.give")) {
			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("give")) {
					if (Bukkit.getPlayer(args[1]) != null) {
						Player target = Bukkit.getPlayer(args[1]);
						try {
							int amount = Integer.parseInt(args[2]);
							if (amount > 0) { 
								if (target.getInventory().firstEmpty() != -1) {
									target.getInventory().addItem(createChunkbuster(amount));
								}
								else {
									target.getWorld().dropItem(target.getLocation(), createChunkbuster(amount));
								}
								target.sendMessage(Utils.cc(busterTargetCommandMessage).replaceAll("%amount%", amount + ""));
								sender.sendMessage(Utils.cc(busterSenderCommandMessage).replaceAll("%amount%", amount + "").replaceAll("%target%", target.getName()));
							}
							else {
								return false;
							}
						} catch (NumberFormatException e) {
							return false;
						}
					}
					else {
						return false;
					}
				}
				else {
					return false;
				}
			}
			else {
				sender.sendMessage(Utils.cc("&eUsage: /chunkbuster give <Player> <Amount>"));
				return false;
			}
		}
		return true;
	}
	
	public ItemStack createChunkbuster(int amount) {
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ItemDataUtil.encodeString("Chunkbuster"));
		for (String s : busterLore) {
			lore.add(Utils.cc(s));
		}
		return Utils.createItem(busterItemType, Utils.cc(busterName), lore, (short) 0, amount);
	}
}
