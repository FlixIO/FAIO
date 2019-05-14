package io.flixion.staffmode;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.flixion.main.Utils;

public class CmdUnfreeze extends BukkitCommand {
	public CmdUnfreeze (String name) {
		super(name);
		this.description = "Unfreeze a frozen player";
		this.usageMessage = "/unfreeze <Player>";
	}
	
	@Override
	public boolean execute (CommandSender sender, String label, String [] args) {
		if (sender.hasPermission("faio.staffmode.use")) {
			if (sender instanceof Player) {
				if (args.length == 1) {
					Player staff = (Player) sender;
					if (Bukkit.getPlayer(args[0]) != null) {
						Player target = Bukkit.getPlayer(args[0]);
						if (StaffHandler.frozenPlayers.containsKey(target.getUniqueId())) {
							StaffHandler.frozenPlayers.get(target.getUniqueId()).cancel();
							StaffHandler.frozenPlayers.remove(target.getUniqueId());
							target.closeInventory();
							staff.sendMessage(Utils.cc("&4&l(!) You have unfrozen &6" + target.getName()));
						}
						else {
							staff.sendMessage(Utils.cc("&4&l(!) This player is not frozen"));
						}
					}
				}
			}
		}
		return true;
	}
}
