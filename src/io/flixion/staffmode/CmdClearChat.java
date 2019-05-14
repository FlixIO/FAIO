package io.flixion.staffmode;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;

public class CmdClearChat extends BukkitCommand {
	private String chatClearBroadcastMessage;
	
	public CmdClearChat(String chatClearBroadcastMessage, String name) {
		super(name);
		this.chatClearBroadcastMessage = chatClearBroadcastMessage;
		this.description = "Clear global chat";
		this.usageMessage = "/clearchat";
		List<String> aliases = new ArrayList<String>();
		aliases.add("cc");
		aliases.add("chatclear");
	}

	public boolean execute (CommandSender sender, String label, String [] args) {
			if (sender.hasPermission("faio.staffmode.clearchat")) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (!p.hasPermission("faio.staffmode.clearchat.bypass")) {
						Bukkit.getScheduler().runTaskAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
							
							@Override
							public void run() {
								for (int i = 0; i < 101; i++) {
									p.sendMessage(" ");
								}
							}
						});
					}
				}
				Bukkit.getScheduler().runTaskLater(FAIOPlugin.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						Bukkit.broadcastMessage(Utils.cc(chatClearBroadcastMessage).replaceAll("%staffMember%", sender.getName()));
					}
				}, 20);
			}
		return true;
	}
}
