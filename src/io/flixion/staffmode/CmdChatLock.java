package io.flixion.staffmode;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import io.flixion.main.Utils;

public class CmdChatLock extends BukkitCommand {
	private String chatLockBroadcastMessage;
	private String chatUnlockBroadcastMessage;

	public CmdChatLock(String chatLockBroadcastMessage, String chatUnlockBroadcastMessage, String name) {
		super(name);
		this.chatLockBroadcastMessage = chatLockBroadcastMessage;
		this.chatUnlockBroadcastMessage = chatUnlockBroadcastMessage;
		this.description = "Lock global chat";
		List<String> aliases = new ArrayList<String>();
		aliases.add("lc");
		aliases.add("chatlock");
		this.setAliases(aliases);
		this.usageMessage = "/lockchat";
	}

	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender.hasPermission("faio.staffmode.lockchat")) {
			if (StaffHandler.isChatLocked) {
				StaffHandler.isChatLocked = false;
				Bukkit.broadcastMessage(
						Utils.cc(chatUnlockBroadcastMessage).replaceAll("%staffMember%", sender.getName()));
			} else {
				StaffHandler.isChatLocked = true;
				Bukkit.broadcastMessage(
						Utils.cc(chatLockBroadcastMessage).replaceAll("%staffMember%", sender.getName()));
			}
		}
		return true;
	}
}
