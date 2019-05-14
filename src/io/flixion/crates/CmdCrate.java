package io.flixion.crates;



import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.flixion.data.PlayerHandler;
import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;

public class CmdCrate extends BukkitCommand {
	
	private String titleFormatText;
	private String createCrateSuccessText;
	private String giveKeyTargetText;
	private String giveKeySenderText;

	public CmdCrate(String titleFormatText, String createCrateSuccessText, String giveKeyTargetText,
			String giveKeySenderText, String name) {
		super(name);
		this.description = "Crate home command";
		this.usageMessage = "/crate";
		this.titleFormatText = titleFormatText;
		this.createCrateSuccessText = createCrateSuccessText;
		this.giveKeyTargetText = giveKeyTargetText;
		this.giveKeySenderText = giveKeySenderText;
	}

	public boolean execute (CommandSender sender, String label, String[] args) {
		if (this.getName().equalsIgnoreCase("crate")) {
			if (sender instanceof Player) {
				if (sender.hasPermission("faio.crates.manage")) {
					Player p = (Player) sender;
					if (args.length == 2) {
						if (args[0].equalsIgnoreCase("create")) {
							CrateObject c = new CrateObject();
							c.createCrate(args[1], p, titleFormatText);
							Crates.getActiveCrates().add(c);
							p.sendMessage(Utils.cc(createCrateSuccessText).replace("%crateName%", args[1]));
						}
						else if (args[0].equalsIgnoreCase("delete")) {
							if (FAIOPlugin.crateNames.contains(args[1])) {
								for (int i = 0; i < Crates.activeCrates.size(); i++) {
									if (Crates.activeCrates.get(i).getName().equals(args[1])) {
										Crates.activeCrates.get(i).getCrateLoc().getWorld().getBlockAt(Crates.activeCrates.get(i).getCrateLoc()).setType(Material.AIR);
										Crates.activeCrates.get(i).getCrateLoc().getWorld().getBlockAt(new Location(Crates.activeCrates.get(i).getCrateLoc().getWorld(), Crates.activeCrates.get(i).getCrateLoc().getBlockX(), Crates.activeCrates.get(i).getCrateLoc().getBlockY() - 1, Crates.activeCrates.get(i).getCrateLoc().getBlockZ())).setType(Material.AIR);
										Crates.activeCrates.get(i).getCrateLoc().getWorld().getBlockAt(new Location(Crates.activeCrates.get(i).getCrateLoc().getWorld(), Crates.activeCrates.get(i).getCrateLoc().getBlockX(), Crates.activeCrates.get(i).getCrateLoc().getBlockY() - 2, Crates.activeCrates.get(i).getCrateLoc().getBlockZ())).setType(Material.AIR);
										Crates.activeCrates.get(i).getHologram().setVisible(true);
										Crates.activeCrates.get(i).getHologram().remove();
										Bukkit.getScheduler().cancelTask(Crates.activeCrates.get(i).getTaskID());
										Crates.activeCrates.remove(i);
										break;
									}
								}
								PlayerHandler.getPlayerData().get(p.getUniqueId()).getCrateKeys().remove(args[1]);
								FAIOPlugin.getCratesConfig().set("crates." + args[1], null);
								FAIOPlugin.saveCratesFile();
								FAIOPlugin.crateNames.remove(args[1]);
								CrateSQL.dropTable(args[1]);
								p.sendMessage(Utils.cc("&eSuccessfully deleted crate: &4" + args[1]));
							}
							else {
								p.sendMessage(Utils.cc("&eThis crate does not exist"));
								return true;
							}
						}
						else {
							crateCommands(p);
						}
					}
					else if (args.length == 4) {
						if (args[0].equalsIgnoreCase("givekey")) {
							if (FAIOPlugin.crateNames.contains(args[1])) {
								if (Bukkit.getPlayer(args[2]) != null) {
									Player target = Bukkit.getPlayer(args[2]);
									try {
										int amount = Integer.parseInt(args[3]);
										if (amount <= 0) {
											p.sendMessage(Utils.cc("&eInvalid number of keys provided"));
											return true;
										}
										PlayerHandler.getPlayerData().get(target.getUniqueId()).getCrateKeys().replace(args[1], PlayerHandler.getPlayerData().get(target.getUniqueId()).getCrateKeys().get(args[1]) + amount);
										CrateSQL.updatePlayerKeys(p, args[1]);
										p.sendMessage(Utils.cc(giveKeySenderText).replaceAll("%crateName%", args[1]).replaceAll("%target%", target.getName().replaceAll("%amount%", amount + "")));
										target.sendMessage(Utils.cc(giveKeyTargetText).replaceAll("%crateName%", args[1]).replaceAll("%amount%", amount + ""));
									} catch (NumberFormatException e) {
										p.sendMessage(Utils.cc("&eInvalid number of keys provided"));
										return true;
									}
								}
								else {
									p.sendMessage(Utils.cc("&eThis player does not exist"));
									return true;
								}
							}
							else {
								p.sendMessage(Utils.cc("&eThis crate does not exist!"));
								return true;
							}
						}
						else {
							crateCommands(p);
						}
					}
					else {
						crateCommands(p);
					}
				}
			}
		}
		return true;
	}
	
	public void crateCommands(Player p) {
		p.sendMessage(Utils.cc("&4------ Crate Key Commands ------"));
		p.sendMessage(Utils.cc(" "));
		p.sendMessage(Utils.cc("&e /crate create <crateName>"));
		p.sendMessage(Utils.cc("&e /crate givekey <crateName> <playerName> <amount>"));
		p.sendMessage(Utils.cc("&e /crate delete <crateName>"));
	}

}
