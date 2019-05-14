package io.flixion.shockwave;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;

import io.flixion.main.ItemDataUtil;

public class ShockwaveTool implements Listener {
	
	@EventHandler (ignoreCancelled=true)
	public void breakArea (BlockBreakEvent e) {
		if (!e.isCancelled()) {
			if (e.getPlayer().getItemInHand() != null) {
				if (e.getPlayer().getItemInHand().hasItemMeta()) {
					if (e.getPlayer().getItemInHand().getItemMeta().hasLore()) {
						if (ItemDataUtil.hasHiddenString(e.getPlayer().getItemInHand().getItemMeta().getLore().get(0))) {
							if (ItemDataUtil.extractHiddenString(e.getPlayer().getItemInHand().getItemMeta().getLore().get(0)).contains("Shockwave")){
								int size = Integer.parseInt(ItemDataUtil.extractHiddenString(e.getPlayer().getItemInHand().getItemMeta().getLore().get(0)).split("#")[1]);
								Location loc = e.getBlock().getLocation();
								for (int i = -size; i < size + 1; i++) {
									for (int j = -size; j < size + 1; j++) {
										for (int k = -size; k < size + 1; k++) {
											Block b = e.getPlayer().getWorld().getBlockAt(new Location(loc.getWorld(), loc.getBlockX() + i, loc.getBlockY() + j, loc.getZ() + k));
											if (b.getType() != Material.BEDROCK && b.getType() != Material.MOB_SPAWNER && b.getType() != Material.AIR && b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST && b.getType() != Material.ENDER_PORTAL_FRAME) {
												if (Board.getInstance().getFactionAt(new FLocation(b)).getTag().equals(FPlayers.getInstance().getByPlayer(e.getPlayer()).getFaction().getTag()) || Board.getInstance().getFactionAt(new FLocation(b)).isWilderness()) {
													b.breakNaturally();
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
