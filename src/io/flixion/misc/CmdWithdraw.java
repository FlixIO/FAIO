package io.flixion.misc;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.ItemDataUtil;
import io.flixion.main.Utils;
import net.milkbowl.vault.economy.EconomyResponse;

public class CmdWithdraw extends BukkitCommand implements Listener {
	private String noteName;
	private ArrayList<String> noteLore;
	private String noteUseMessage;
	private String withdrawFailMessage;
	private String withdrawSuccessMessage;
	private Material noteItemType;

	public CmdWithdraw(String name, String noteName, ArrayList<String> noteLore, String noteUseMessage,
			String withdrawFailMessage, String withdrawSuccessMessage, Material noteItemType) {
		super(name);
		this.noteName = noteName;
		this.noteLore = noteLore;
		this.noteUseMessage = noteUseMessage;
		this.withdrawFailMessage = withdrawFailMessage;
		this.withdrawSuccessMessage = withdrawSuccessMessage;
		this.noteItemType = noteItemType;
		description = "Use to withdraw an amount of money into an item voucher";
		usageMessage = "/withdraw <Amount>";
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (sender instanceof Player) {
			if (sender.hasPermission("faio.withdraw")) {
				Player p = (Player) sender;
				if (args.length == 1) {
					try {
						long amount = Long.parseLong(args[0]);
						if (amount > 0) {
							EconomyResponse rs = FAIOPlugin.getEco().withdrawPlayer(p.getName(), amount);
							if (rs.transactionSuccess()) {
								if (p.getInventory().firstEmpty() != -1) {
									p.getInventory().addItem(createBanknote(amount));
								}
								else {
									p.getWorld().dropItem(p.getLocation(), createBanknote(amount));
								}
								p.sendMessage(Utils.cc(withdrawSuccessMessage).replaceAll("%amount%", amount + ""));
							}
							else {
								p.sendMessage(Utils.cc(withdrawFailMessage));
							}
						}
						else {
							p.sendMessage(Utils.cc(withdrawFailMessage));
						}
					} catch (NumberFormatException e) {
						p.sendMessage(Utils.cc(withdrawFailMessage));
					}
				}
				else {
					sender.sendMessage(Utils.cc("&eUsage: /withdraw <Amount>"));
					return false;
				}
			}
		}
		return true;
	}
	
	public ItemStack createBanknote (long amount) {
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ItemDataUtil.encodeString("Banknote#" + amount));
		for (String s : noteLore) {
			lore.add(Utils.cc(s).replaceAll("%amount%", amount + ""));
		}
		return Utils.createItem(noteItemType, Utils.cc(noteName).replaceAll("%amount%", amount + ""), lore, (short) 0, 1);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void useBanknote (PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getPlayer().getItemInHand() != null) {
				if (e.getPlayer().getItemInHand().getType() == noteItemType) {
					if (e.getPlayer().getItemInHand().hasItemMeta()) {
						if (e.getPlayer().getItemInHand().getItemMeta().hasLore()) {
							if (ItemDataUtil.hasHiddenString(e.getPlayer().getItemInHand().getItemMeta().getLore().get(0))) {
								if (ItemDataUtil.extractHiddenString(e.getPlayer().getItemInHand().getItemMeta().getLore().get(0)).contains("Banknote")) {
									long amount = Long.parseLong(ItemDataUtil.extractHiddenString(e.getPlayer().getItemInHand().getItemMeta().getLore().get(0)).split("#")[1]);
									FAIOPlugin.getEco().depositPlayer(e.getPlayer().getName(), amount);
									e.getPlayer().sendMessage(Utils.cc(noteUseMessage).replaceAll("%amount%", amount + ""));
									if (e.getPlayer().getItemInHand().getAmount() > 1) {
										e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand().getAmount() - 1);
									}
									else {
										e.getPlayer().getInventory().clear(e.getPlayer().getInventory().getHeldItemSlot());
									}
									e.setCancelled(true);
								}
							}
						}
					}
				}
			}
		}
	}
}
