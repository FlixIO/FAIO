package io.flixion.misc;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import io.flixion.main.Utils;

public class WebLimiter implements Listener {
	private int allowedWebCount;
	private String failedWebplace;

	public WebLimiter(int allowedWebCount, String failedWebplace) {
		super();
		this.allowedWebCount = allowedWebCount;
		this.failedWebplace = failedWebplace;
	}

	@EventHandler
	public void checkWebs (BlockPlaceEvent e) {
		if (e.getBlock().getType() == Material.WEB) {
			if (!checkAxis(e.getBlock().getChunk(), e.getBlock().getX(), e.getBlock().getZ())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(Utils.cc(failedWebplace));
			}
		}
	}
	
	public boolean checkAxis (Chunk c, int x, int z) {
		int webCount = 0;
		for (int i = 0; i < 256; i++) {
			if (c.getBlock(x, i, z).getType() == Material.WEB) {
				webCount++;
			}
		}
		if (webCount <= allowedWebCount) {
			return true;
		}
		else {
			return false;
		}
	}
}
