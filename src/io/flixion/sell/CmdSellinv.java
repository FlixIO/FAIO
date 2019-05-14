package io.flixion.sell;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.MaterialEnumUtil;
import io.flixion.main.Utils;

public class CmdSellinv extends BukkitCommand {
	private int radius;
	private String selltext;
	private HashMap<String, Double> sellableItems;
	
	public CmdSellinv(int radius, HashMap<String, Double> sellableItems, String sellText, String name) {
		super(name);
		this.description = "Sell all items in a an inventory";
		this.usageMessage = "/sellinv";
		this.radius = radius;
		this.sellableItems = sellableItems;
		this.selltext = sellText;
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
							if (sellableItems.containsKey(i.getType().toString())) {
								amount += i.getAmount() * sellableItems.get(i.getType().toString());
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
									.replace("%money%", e.getValue() * sellableItems.get(e.getKey()) + ""));
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
