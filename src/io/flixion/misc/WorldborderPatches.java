package io.flixion.misc;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class WorldborderPatches implements Listener	{
	private boolean stackOnBorder;
	
	public WorldborderPatches(boolean stackOnBorder) {
		super();
		this.stackOnBorder = stackOnBorder;
	}

	@EventHandler (ignoreCancelled=true)
	public void liquidPastWorldBorder (BlockFromToEvent e) {
		if (e.getBlock().getType().toString().contains("WATER")|| e.getBlock().getType().toString().contains("LAVA")) {
			double size = e.getBlock().getWorld().getWorldBorder().getSize() / 2;
			if (Math.abs(e.getToBlock().getLocation().getBlockX()) - 1 >= size || Math.abs(e.getToBlock().getLocation().getBlockZ()) - 1 >= size) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void floatingEntity (EntityChangeBlockEvent e) {
		if (!stackOnBorder) {
			if (e.getEntity().getType() == EntityType.FALLING_BLOCK) {
				double size = e.getBlock().getWorld().getWorldBorder().getSize() / 2;
				if (Math.abs(e.getBlock().getLocation().getBlockX()) == size || Math.abs(e.getBlock().getLocation().getBlockZ()) == size) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void throwPearl (PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.ENDER_PEARL) {
			double size = e.getPlayer().getWorld().getWorldBorder().getSize() / 2;
			if (Math.abs(e.getTo().getBlockX()) >= size - 1|| Math.abs(e.getTo().getBlockZ()) >= size - 1) {
				e.setCancelled(true);
			}
		}
	}
}
