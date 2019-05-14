package io.flixion.staffmode;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.flixion.main.Utils;

public class CmdFreeze extends BukkitCommand {

	public CmdFreeze(String name) {
		super(name);
		this.description = "Freeze a player";
		this.usageMessage = "/freeze <Player>";
	}

	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender.hasPermission("faio.staffmode.use")) {
			if (sender instanceof Player) {
				if (args.length == 1) {
					Player staff = (Player) sender;
					if (Bukkit.getPlayer(args[0]) != null) {
						Player target = Bukkit.getPlayer(args[0]);
						if (StaffHandler.frozenPlayers.containsKey(target.getUniqueId())) {
							staff.sendMessage(Utils.cc("&4&l(!) This player is already frozen"));
						} else {
							StaffHandler.initFreeze(target);
							staff.sendMessage(Utils.cc("&4&l(!) You have frozen &6" + target.getName()));
						}
					}
				}
			}
		}
		return true;
	}
}
