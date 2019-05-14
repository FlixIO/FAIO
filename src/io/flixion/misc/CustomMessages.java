package io.flixion.misc;

import java.util.ArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.flixion.main.Utils;

public class CustomMessages implements Listener{
	private ArrayList<String> joinMessage;
	private String leaveMessage;
	
	public CustomMessages(ArrayList<String> joinMessage, String leaveMessage) {
		super();
		this.joinMessage = joinMessage;
		this.leaveMessage = leaveMessage;
	}

	@EventHandler (ignoreCancelled=true)
	public void onJoin (PlayerJoinEvent e) {
		e.setJoinMessage(null);
		for (String s : joinMessage) {
			e.getPlayer().sendMessage(Utils.cc(s).replaceAll("%player%", e.getPlayer().getName()));
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void onLeave (PlayerQuitEvent e) {
		e.setQuitMessage(Utils.cc(leaveMessage).replaceAll("%player%", e.getPlayer().getName()));
	}
}
