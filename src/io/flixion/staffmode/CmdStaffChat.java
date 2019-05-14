package io.flixion.staffmode;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.flixion.main.Utils;

public class CmdStaffChat extends BukkitCommand {

	public CmdStaffChat(String name) {
		super(name);
		this.description = "Send messages via the staffchat";
		List<String> aliases = new ArrayList<String>();
		aliases.add("sc");
		this.setAliases(aliases);
		this.usageMessage = "/sc <Message...>";
	}

	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender.hasPermission("faio.staffmode.chat")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (args.length > 0) {
					StringBuilder message = new StringBuilder();
					for (String s : args) {
						message.append(s + " ");
					}
					for (Player p1 : Bukkit.getOnlinePlayers()) {
						if (p1.hasPermission("faio.staffmode.chat")) {
							p1.sendMessage(Utils.cc("&8[&4Staff&8] &6" + p.getName() + ": &a" + message.toString()));
						}
					}
				}
			}
		}
		return true;
	}
}
