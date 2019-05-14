package io.flixion.sell;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.earth2me.essentials.Worth;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.MaterialEnumUtil;
import io.flixion.main.Utils;

public class CmdSellinvEssentials extends BukkitCommand {
	private int radius;
	private String selltext;
	Worth w;
	
	public CmdSellinvEssentials(int radius, String sellText, String name) {
		super(name);
		this.description = "Sell all items in a an inventory";
		this.usageMessage = "/sellinv";
		this.radius = radius;
		this.selltext = sellText;
		File f = new File(Bukkit.getPluginManager().getPlugin("Essentials").getDataFolder().getPath());
		if (f.exists()) {
			w = new Worth(f);
		}
	}
	
	public boolean checkFactionZone(Location loc, FPlayer p) {
		if (!Board.getInstance().getFactionAt(new FLocation(loc)).getId().equals(p.getFactionId())) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean execute(CommandSender sender, String label, String[] args) {
		if (this.getName().equalsIgnoreCase("sellinv")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (p.hasPermission("faio.sellinv")) {
					double amount = 0;
					HashMap<String, Integer> itemsSold = new HashMap<>();
					int index = 0;
					for (ItemStack i : p.getInventory().getContents()) {
						if (i != null) {
							if (w.getPrice(i) != null) {
								amount += i.getAmount() * Double.parseDouble(w.getPrice(i).toString());
								if (itemsSold.containsKey(i.getType().toString())) {
									itemsSold.replace(i.getType().toString(),
											itemsSold.get(i.getType().toString()) + i.getAmount());
								} else {
									itemsSold.put(i.getType().toString(), i.getAmount());
								}
								p.getInventory().clear(index);
								p.updateInventory();
							}
						}
						index++;
					}
					if (itemsSold.size() > 0) {
						for (Map.Entry<String, Integer> e : itemsSold.entrySet()) {
							p.sendMessage(Utils.cc(selltext).replace("%amount%", e.getValue().toString())
									.replace("%item%", MaterialEnumUtil.valueOf(e.getKey()).firstAllUpperCased())
									.replace("%money%", e.getValue() * Double.parseDouble(w.getPrice(new ItemStack(Material.valueOf(e.getKey()))).toString()) + ""));
						}
						FAIOPlugin.getEco().depositPlayer(p, amount);
						p.sendMessage(Utils.cc("&8&l(!) &eItems sold for a total of &a$" + amount));
					} else {
						p.sendMessage(Utils.cc("&4&l(!) There are no sellable items in your inventory!"));
					}
				}
			}
		}
		return true;
	}
}
