package io.flixion.misc;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class WaterInteractRedstone implements Listener{
	@EventHandler (ignoreCancelled=true)
	public void waterFlowAgainstRedstone (BlockFromToEvent e) {
		if (e.getBlock().getType() == Material.WATER || e.getBlock().getType() == Material.STATIONARY_WATER) {
			if (e.getToBlock().getType().toString().contains("DIODE") || e.getToBlock().getType().toString().contains("REDSTONE")) {
				e.setCancelled(true);
			}
		}
	}
}
