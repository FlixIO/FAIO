package io.flixion.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.events.PermissionEntityEvent;

public class PermissionsExHookEvents implements Listener{
	@EventHandler (ignoreCancelled=true)
	public void balanceUpdate (PermissionEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getAction() == ru.tehkode.permissions.events.PermissionEntityEvent.Action.RANK_CHANGED){
				String rankPrefix = ChatColor.stripColor(Utils.cc(PermissionsEx.getPermissionManager().getUser((Player) e.getEntity()).getPrefix()));
				if (FAIOPlugin.scoreboardEnabled) {
					ScoreboardHandler.playerScoreboards.get(((Player)e.getEntity()).getUniqueId()).getPlayerRank().setSuffix(Utils.cc(" &f" + rankPrefix));
				}
			}
		}
	}
}
