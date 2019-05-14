package io.flixion.misc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import io.flixion.main.FAIOPlugin;

public class EmptyBottleRemover implements Listener	{
	@EventHandler (ignoreCancelled=true)
	public void removeBottle (PlayerItemConsumeEvent e) {
		if (e.getItem().getType() == Material.POTION) {
			Bukkit.getScheduler().runTaskLater(FAIOPlugin.getInstance(), new Runnable() {
				
				@Override
				public void run() {
					e.getPlayer().getInventory().remove(new ItemStack(Material.GLASS_BOTTLE));
				}
			}, 5);
		}
	}
}
