package io.flixion.misc;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.flixion.main.Utils;

public class NightVision extends BukkitCommand {
	private String enabledNightVision;
	private String disabledNightVision;
	
	public NightVision(String enabledNightVision, String disabledNightVision, String name) {
		super(name);
		this.description = "Enable nightvision for the player";
		this.usageMessage = "/nv";
		this.enabledNightVision = enabledNightVision;
		this.disabledNightVision = disabledNightVision;
	}

	public boolean execute (CommandSender sender, String label, String [] args) {
		if (this.getName().equalsIgnoreCase("nv")) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (p.hasPermission("faio.nightvision")) {
					if (p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
						p.removePotionEffect(PotionEffectType.NIGHT_VISION);
						p.sendMessage(Utils.cc(disabledNightVision));
					}
					else {
						p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 1, false));
						p.sendMessage(Utils.cc(enabledNightVision));
					}
				}
			}
		}
		return true;
	}
}
