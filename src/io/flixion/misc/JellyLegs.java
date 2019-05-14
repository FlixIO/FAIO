package io.flixion.misc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class JellyLegs implements Listener{
	@EventHandler (ignoreCancelled=true)
	public void cancelFallDamage (EntityDamageEvent e) {
		if (e.getCause() == DamageCause.FALL) {
			if (e.getEntity() instanceof Player) {
				if (((Player) e.getEntity()).hasPermission("faio.jellylegs")){
					e.setCancelled(true);
				}
			}
		}
	}
}
