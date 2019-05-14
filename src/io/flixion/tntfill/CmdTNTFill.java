package io.flixion.tntfill;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

import io.flixion.main.Utils;

public class CmdTNTFill extends BukkitCommand {
	private int radius;
	private String successFillText;
	private String failFillText;
	private String noDispenserText;
	
	public CmdTNTFill (int radius, String success, String failed, String noDispensers, String name) {
		super(name);
		this.radius = radius;
		this.successFillText = success;
		this.failFillText = failed;
		this.noDispenserText = noDispensers;
		this.description = "Fill nearby dispensers with TNT";
		this.usageMessage = "/tntfill <Amount>";
	}
	
	public boolean checkFactionZone(Location loc, FPlayer p) {
		if (!Board.getInstance().getFactionAt(new FLocation(loc)).getId().equals(p.getFactionId()) && !Board.getInstance().getFactionAt(new FLocation(loc)).isWilderness()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public int fillDispenserContents (Dispenser d, int amountCheck) {
		int amount = 0;
		for (ItemStack i : d.getInventory().getContents()) {
			if (i != null && i.getType() == Material.TNT) {
				amount += i.getAmount();
			}
		}
		if (amount >= amountCheck) {
			return -1;
		}
		else {
			return amountCheck - amount;
		}
	}
	
	public boolean execute (CommandSender sender, String label, String [] args) {
		if (this.getName().equalsIgnoreCase("tntfill")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (p.hasPermission("faio.tntfill")) {
					int amount = 1;
					int totalDispensers = 0;
					int filledDispeners = 0;
					if (args.length == 1) { 
						try {
							amount = Integer.parseInt(args[0]);
						} catch (NumberFormatException e){
						}
					}
					int playerTNT = 0;
					for (int i = 0; i < p.getInventory().getSize(); i++) {
						ItemStack item = p.getInventory().getItem(i); 
						if (item != null && item.getType() == Material.TNT) {
							playerTNT += item.getAmount();
							p.getInventory().clear(i);
						}
					}
					for (int x = -(radius + 1); x < radius + 1; x++) {
						for (int y = -(radius + 1); y < radius + 1; y++) {
							for (int z = -(radius + 1); z < radius + 1; z++) {
								if (p.getWorld().getBlockAt(new Location(p.getWorld(), p.getLocation().getBlockX() + x, p.getLocation().getBlockY() + y, p.getLocation().getBlockZ() + z)).getState() instanceof Dispenser) {
									Dispenser d = (Dispenser) p.getWorld().getBlockAt(new Location(p.getWorld(), p.getLocation().getBlockX() + x, p.getLocation().getBlockY() + y, p.getLocation().getBlockZ() + z)).getState();
									if (checkFactionZone(d.getLocation(), FPlayers.getInstance().getByPlayer(p))) {
										continue;
									}
									else {
										totalDispensers++;
										int amountToFill = fillDispenserContents(d, amount);
										if (amountToFill == -1) {
											totalDispensers--;
											continue;
										}
										else {
											if (p.hasPermission("faio.tntfill.unlimited")) {
												filledDispeners++;
												d.getInventory().addItem(new ItemStack(Material.TNT, amountToFill));
											}
											else if (playerTNT - amountToFill >= 0) {
												filledDispeners++;
												d.getInventory().addItem(new ItemStack(Material.TNT, amountToFill));
												d.update();
												playerTNT -= amountToFill;
											}
											else {
												continue;
											}
										}
									}
								}
							}
						}
					}
					if (playerTNT > 0) {
						p.getInventory().addItem(new ItemStack(Material.TNT, playerTNT));
					}
					if (totalDispensers > 0) {
						p.sendMessage(Utils.cc(successFillText).replace("%amountFilled%", filledDispeners + "").replace("%fillAmount%", amount + ""));
						p.sendMessage(Utils.cc(failFillText).replace("%amountNotFilled%", totalDispensers - filledDispeners + "").replace("%fillAmount%", amount + ""));
					}
					else {
						p.sendMessage(Utils.cc(noDispenserText));
					}
				}
			}
		}
		return true;
	}
}
