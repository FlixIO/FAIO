package io.flixion.scoreboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;
import net.ess3.api.events.UserBalanceUpdateEvent;

public class EssentialsHookEvents implements Listener{
	@EventHandler (ignoreCancelled=true)
	public void balanceUpdate (UserBalanceUpdateEvent e) {
		if (FAIOPlugin.scoreboardEnabled) {
			ScoreboardHandler.playerScoreboards.get(e.getPlayer().getUniqueId()).getPlayerBalance().setSuffix(Utils.cc(" &f$" + e.getNewBalance().longValue()));
		}
	}
}
