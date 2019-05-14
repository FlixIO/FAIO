package io.flixion.ftop;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;

public class CmdRefreshFTop implements Listener{
	
	private FTop ftop;
	public CmdRefreshFTop(FTop ftop) {
		super();
		this.ftop = ftop;
	}

	@EventHandler (ignoreCancelled=true)
	public void refreshCommand (PlayerCommandPreprocessEvent e) {
		if (e.getMessage().equalsIgnoreCase("/ftop refresh") && e.getPlayer().hasPermission("faio.ftop.refresh")) {
			Bukkit.getScheduler().cancelTask(FAIOPlugin.ftopTaskID);
			FAIOPlugin.initFtop(ftop, false);
			e.getPlayer().sendMessage(Utils.cc("&4&l(!) Re-initializing f top values"));
			e.setCancelled(true);
		}
	}
}
