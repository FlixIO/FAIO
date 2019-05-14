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

public class CmdUnTNTFill extends BukkitCommand {
	private int radius;
	private String unFillText;
	private String noNearbyDispensers;
	private String noInventorySpace;
	
	
	public CmdUnTNTFill(int radius, String unFillText, String noNearbyDispensers, String noInventorySpace, String name) {
		super(name);
		this.description = "Remove TNT from nearby dispensers";
		this.usageMessage = "/untntfill";
		this.radius = radius;
		this.unFillText = unFillText;
		this.noNearbyDispensers = noNearbyDispensers;
		this.noInventorySpace = noInventorySpace;
	}

	public boolean checkFactionZone(Location loc, FPlayer p) {
		if (!Board.getInstance().getFactionAt(new FLocation(loc)).getId().equals(p.getFactionId()) && !Board.getInstance().getFactionAt(new FLocation(loc)).isWilderness()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public int checkInventorySpace(Player p) {
		int availableSpace = 0;
		for (int i = 0; i < p.getInventory().getSize(); i++) {
			ItemStack item = p.getInventory().getItem(i);
			if (item == null) {
				availableSpace += 64;
			}
		}
		return availableSpace;
	}
	
	public boolean execute (CommandSender sender, String label, String [] args) {
		if (this.getName().equalsIgnoreCase("untntfill")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (p.hasPermission("faio.untntfill")) {
					int playerSpace = checkInventorySpace(p);
					int initSpace = playerSpace;
					if (playerSpace == 0) {
						p.sendMessage(Utils.cc(noInventorySpace));
						return true;
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
										if (playerSpace >= 0 ) {
											for (int i = 0; i < d.getInventory().getSize(); i++) {
												ItemStack item = d.getInventory().getItem(i);
												if (item != null && item.getType() == Material.TNT) {
													if (playerSpace - item.getAmount() >= 0) {
														playerSpace -= item.getAmount();
														d.getInventory().clear(i);
													}
													else {
														break;
													}
												}
											}
										}
										else {
											continue;
										}
									}
								}
							}
						}
					}
					if (playerSpace >= 0) {
						if (initSpace - playerSpace != 0) {
							p.getInventory().addItem(new ItemStack(Material.TNT, initSpace - playerSpace));
							p.sendMessage(Utils.cc(unFillText).replace("%unFilledAmount%", initSpace - playerSpace + ""));
						}
						else {
							p.sendMessage(Utils.cc(noNearbyDispensers));
						}
					}
					else {
						p.sendMessage(Utils.cc(noNearbyDispensers));
					}
				}
			}
		}
		return true;
	}
}
