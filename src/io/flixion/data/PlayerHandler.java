package io.flixion.data;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import io.flixion.crates.CrateSQL;
import io.flixion.main.FAIOPlugin;

public class PlayerHandler implements Listener{
	private static HashMap<UUID, PlayerProfile> playerData = new HashMap<>();
	
	@EventHandler (ignoreCancelled=true)
	public void initPlayerProfile(PlayerJoinEvent e) {
		PlayerHandler.getPlayerData().put(e.getPlayer().getUniqueId(), new PlayerProfile(e.getPlayer().getUniqueId(), null));
		if (FAIOPlugin.cratesImport) {
			CrateSQL.getPlayerKeys(e.getPlayer());
		}
	}

	public static HashMap<UUID, PlayerProfile> getPlayerData() {
		return playerData;
	}

	public static void setPlayerData(HashMap<UUID, PlayerProfile> playerData) {
		PlayerHandler.playerData = playerData;
	}
	
}
