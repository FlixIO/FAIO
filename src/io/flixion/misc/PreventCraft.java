package io.flixion.misc;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import io.flixion.main.Utils;

public class PreventCraft implements Listener {
	ArrayList<Material> uncraftableItems;
	String cannotCraftItemText;
	
	public PreventCraft(ArrayList<Material> uncraftableItems, String cannotCraftItemText) {
		super();
		this.uncraftableItems = uncraftableItems;
		this.cannotCraftItemText = cannotCraftItemText;
	}


	@EventHandler (ignoreCancelled=true)
	public void cancelCraft (CraftItemEvent e) {
		if (uncraftableItems.contains(e.getRecipe().getResult().getType())) {
			e.setCancelled(true);
			((Player) e.getWhoClicked()).sendMessage(Utils.cc(cannotCraftItemText));
		}
	}
}
