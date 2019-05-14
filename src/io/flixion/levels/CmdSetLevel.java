package io.flixion.levels;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;

public class CmdSetLevel extends BukkitCommand{
	public CmdSetLevel (String name) {
		super(name);
		this.description = "Set a factions f level";
		this.usageMessage = "/fsetlevel <Faction> <Level>";
	}
	
	@SuppressWarnings("deprecation")
	public boolean execute (CommandSender sender, String label, String [] args) {
		if (this.getName().equalsIgnoreCase("fsetlevel")) {
			if (sender instanceof Player) {
				if (sender.hasPermission("faio.fsetlevel")) {
					if (args.length == 2) {
						Player p = (Player) sender; 
						if (Factions.getInstance().getByTag(args[0]) != null) {
							try {
								if (Integer.parseInt(args[1]) > 0 && Integer.parseInt(args[1]) < 11) {
									Faction f = Factions.getInstance().getByTag(args[0]);
									Levels.activeFactions.get(f.getTag()).setLevel(Integer.parseInt(args[1]));
									if (Integer.parseInt(args[1]) == 1) {
										Levels.activeFactions.get(f.getTag()).setEXP(0);
									}
									else { 
										Levels.activeFactions.get(f.getTag()).setEXP(Levels.getUpgradeCosts().get(Integer.parseInt(args[1])));
									}
									for (FPlayer fp : f.getFPlayers()) {
										int taskID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(FAIOPlugin.getInstance(), new Runnable() {
											
											@Override
											public void run() {
												if (!fp.getFaction().isWilderness() && !fp.getFaction().isSafeZone() && !fp.getFaction().isWarZone()) {
													if (Levels.activeFactions.get(fp.getFaction().getTag()).getLevel() < 10) {
														Levels.activeFactions.get(fp.getFaction().getTag()).setEXP(Levels.activeFactions.get(fp.getFaction().getTag()).getEXP() + Levels.expPerMinute);
														if (Levels.getUpgradeCosts().get(Levels.activeFactions.get(fp.getFaction().getTag()).getLevel() + 1) <= Levels.activeFactions.get(fp.getFaction().getTag()).getEXP()) {
															Levels.activeFactions.get(fp.getFaction().getTag()).setLevel(Levels.activeFactions.get(fp.getFaction().getTag()).getLevel() + 1);
															Bukkit.broadcastMessage(Utils.cc(Levels.broadcastFactionUpgradeNotification).replaceAll("%faction%", fp.getFaction().getTag()).replaceAll("%level%", Levels.activeFactions.get(fp.getFaction().getTag()).getLevel() + ""));
														}
													}
													else {
														Bukkit.getScheduler().cancelTask(Levels.activeFactions.get(fp.getFaction().getTag()).getGeneratingPlayerTaskIDs().get(fp.getAccountId()));
													}
												}
												else {
													Bukkit.getScheduler().cancelTask(Levels.activeFactions.get(fp.getFaction().getTag()).getGeneratingPlayerTaskIDs().get(fp.getAccountId()));
												}
											}
											
										}, 40, 20 * 60);
										Levels.activeFactions.get(f.getTag()).getGeneratingPlayerTaskIDs().put(fp.getAccountId(), taskID);
									}
									p.sendMessage(Utils.cc("&4&l(!) &eYou have set the faction level of &a" + f.getTag() + " &eto level &4" + args[1]));
								}
								else {
									p.sendMessage(Utils.cc("&4Usage: /fsetlevel <factionName> <Level>"));
								}
							} catch (NumberFormatException e) {
								p.sendMessage(Utils.cc("&4Usage: /fsetlevel <factionName> <Level>"));
							}
						}
						else {
							p.sendMessage(Utils.cc("&4(!) This faction does not exist"));
						}
					}
				}
			}
		}
		return true;
	}
}
