package io.flixion.ftop;

import java.text.DecimalFormat;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.massivecraft.factions.Factions;

import io.flixion.main.SpawnerEnumUtil;
import io.flixion.main.Utils;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CmdFTop implements Listener{
	private String displayInformationFormat;
	DecimalFormat formatter = new DecimalFormat("#,###.00");
	
	public CmdFTop(String displayInformationFormat) {
		super();
		this.displayInformationFormat = displayInformationFormat;
	}

	@EventHandler (ignoreCancelled=true)
	public void callFTop(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().startsWith("/f top")) {
			int page = 1;
			try {
				if (e.getMessage().replace("/", "").split(" ").length < 3) {
					page = 1;
				}
				else {
					page = Integer.parseInt(e.getMessage().split(" ")[2]);
				}
				if (page <= 0) {
					page = 1;
				}
				if (Integer.parseInt(page + "0") - 10 > FTop.topFactionsOrdered.size()) {
					page = 1;
				}
			} catch (NumberFormatException e1) {
			}
			if (Factions.getInstance().getAllFactions().size() - 3 > 0) {
				if (FTop.topFactionsOrdered.size() > 0) {
					int i = Integer.parseInt(page + "0") - 10;
					e.getPlayer().sendMessage(Utils.cc("&f&m----------&4&lFaction Top Values&f&m----------"));
					e.getPlayer().sendMessage(Utils.cc(" "));
					for (Map.Entry<String, FTopObjects> entry : FTop.topFactionsOrdered.entrySet()) {
						if (i > Integer.parseInt(page + "0")) { //i = rank // entry.getKey = faction name // (long) getValue = value
							break;
						}
						TextComponent ftop = new TextComponent(Utils.cc(displayInformationFormat).replaceAll("%rank%", (i + 1) + "").replaceAll("%factionName%", entry.getKey()).replaceAll("%value%", formatter.format(entry.getValue().getValue())));
						StringBuilder s = new StringBuilder();
						s.append("&ePlayer Wealth: $" + formatter.format(entry.getValue().getPlayerWealthValue()) + "\n");
						s.append("&eSpawner & Block Wealth: $" + formatter.format(entry.getValue().getValue() - entry.getValue().getPlayerWealthValue()) + "\n\n");
						s.append("&eFaction Spawner Breakdown \n\n");
						for (Map.Entry<String, Integer> entry2 : entry.getValue().getSpawnerCount().entrySet()) {
							try {
								s.append("&6>> " + SpawnerEnumUtil.valueOf(entry2.getKey()).firstAllUpperCased() + " &cx" + entry2.getValue() + "\n");
							} catch (IllegalArgumentException e1) {
								s.append("&6>> " + entry2.getKey().replaceAll("_", "") + " &cx" + entry2.getValue() + "\n");
							}
						}
						ftop.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Utils.cc(s.toString())).create()));
						e.getPlayer().spigot().sendMessage(ftop);
						i++;
					}
					e.getPlayer().sendMessage(Utils.cc(" "));
					e.getPlayer().sendMessage(Utils.cc("&e >> Showing " + FTop.topFactionsOrdered.size() + "/" + (Factions.getInstance().getAllFactions().size() - 3) + " faction values"));
					e.getPlayer().sendMessage(Utils.cc("&e >> View a faction's spawner breakdown by hovering above their name"));
					e.getPlayer().sendMessage(Utils.cc("&f&m----------------------------------------"));
				}
				else {
					e.getPlayer().sendMessage(Utils.cc("&4&l(!) Ftop values are still processing!")); 
				}
			}
			else {
				e.getPlayer().sendMessage(Utils.cc("&4&l(!) There are no factions to display on f top!")); 
			}
			e.setCancelled(true);
		}
	}
}
