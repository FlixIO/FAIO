package io.flixion.misc;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;

public class AutoBroadcaster {
	private ArrayList<String> messagesToBroadcast;
	private String broadcastHeader;
	private String broadcastFooter;
	private String broadcastPrefix;
	private int broadcastInterval;
	
	public AutoBroadcaster(ArrayList<String> messagesToBroadcast, String broadcastHeader, String broadcastFooter,
			String broadcastPrefix, int broadcastInterval) {
		super();
		this.messagesToBroadcast = messagesToBroadcast;
		this.broadcastHeader = broadcastHeader;
		this.broadcastFooter = broadcastFooter;
		this.broadcastPrefix = broadcastPrefix;
		this.broadcastInterval = broadcastInterval;
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(FAIOPlugin.getInstance(), new Runnable() {
			int i = 0;
			@Override
			public void run() {
				if (i < messagesToBroadcast.size()) {
					Bukkit.broadcastMessage(Utils.cc(broadcastHeader));
					Bukkit.broadcastMessage(Utils.cc(broadcastPrefix + " " + messagesToBroadcast.get(i)));
					Bukkit.broadcastMessage(Utils.cc(broadcastFooter));
					i++;
				}
				else {
					i = 0;
				}
			}
		}, 100, 20 * broadcastInterval);
	}
	
	
}
