package io.flixion.hopper;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

public class CropHopper implements Listener {
	private HashMap<String, Hopper> hopperCache = new HashMap<>();
	
	@EventHandler
	public void removeHopper (BlockBreakEvent e) {
		if (e.getBlock().getType() == Material.HOPPER) {
			if (hopperCache.containsKey(e.getBlock().getLocation().getChunk().getX() + ":" + e.getBlock().getLocation().getChunk().getZ())) {
				hopperCache.remove(e.getBlock().getLocation().getChunk().getX() + ":" + e.getBlock().getLocation().getChunk().getZ());
			}
		}
	}
	
	@EventHandler
	public void entityItemSpawn (ItemSpawnEvent e) {
		if (e.getEntity().getItemStack().getType() == Material.CACTUS) {
			if (hopperCache.containsKey(e.getLocation().getChunk().getX() + ":" + e.getLocation().getChunk().getZ())) {
				if (hopperCache.get(e.getLocation().getChunk().getX() + ":" + e.getLocation().getChunk().getZ()).getInventory().firstEmpty() != -1) {
					hopperCache.get(e.getLocation().getChunk().getX() + ":" + e.getLocation().getChunk().getZ()).getInventory().addItem(e.getEntity().getItemStack());
					e.getEntity().remove();
				}
				else {
					hopperCache.remove(e.getLocation().getChunk().getX() + ":" + e.getLocation().getChunk().getZ());
					if (checkHopper(e.getLocation().getChunk(), e.getLocation().getBlockY() + 1)) {
						hopperCache.get(e.getLocation().getChunk().getX() + ":" + e.getLocation().getChunk().getZ()).getInventory().addItem(e.getEntity().getItemStack());
						e.getEntity().remove();
					}
				}
			}
			else {
				if (checkHopper(e.getLocation().getChunk(), e.getLocation().getBlockY() + 1)) {
					hopperCache.get(e.getLocation().getChunk().getX() + ":" + e.getLocation().getChunk().getZ()).getInventory().addItem(e.getEntity().getItemStack());
					e.getEntity().remove();
				}
			}
		}
	}
	
	public boolean checkHopper(Chunk c, int maxHeight) {
		boolean valid = false;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 256; j++) {
				for (int z = 0; z < 16; z++) {
					if (c.getBlock(i, j, z).getType() == Material.HOPPER) {
						if (c.getBlock(i, j, z).getState() instanceof Hopper) {
							Hopper h = (Hopper) c.getBlock(i, j, z).getState();
							if (h.getInventory().firstEmpty() == -1) {
								continue;
							}
							else {
								hopperCache.put(c.getX() + ":" +c.getZ(), (Hopper) c.getBlock(i, j, z).getState());
								valid = true;
							}
						}
					}
				}
			}
		}
		return valid;
	}
}
