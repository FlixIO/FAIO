package io.flixion.scoreboard;

import org.bukkit.Bukkit;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.event.EventBus;
import me.lucko.luckperms.api.event.user.track.UserDemoteEvent;
import me.lucko.luckperms.api.event.user.track.UserPromoteEvent;

public class LuckPermsHook {
	
	public LuckPermsHook () {
		LuckPermsApi api = LuckPerms.getApi();
		EventBus eventBus = api.getEventBus();
		eventBus.subscribe(UserPromoteEvent.class, this::onUserPromote);
		eventBus.subscribe(UserDemoteEvent.class, this::onUserDemote);
	}
	
	public void onUserPromote (UserPromoteEvent e) {
		Bukkit.getScheduler().runTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				ScoreboardHandler.playerScoreboards.get(e.getUser().getUuid()).getPlayerRank().setSuffix(Utils.cc(" &f" + e.getGroupTo().get()));
			}
		});
	}
	
	public void onUserDemote (UserDemoteEvent e) {
		Bukkit.getScheduler().runTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				ScoreboardHandler.playerScoreboards.get(e.getUser().getUuid()).getPlayerRank().setSuffix(Utils.cc(" &f" + e.getGroupTo().get()));
			}
		});
	}
}
