package io.flixion.chunkbuster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.ItemDataUtil;
import io.flixion.main.Utils;

public class ChunkBuster implements Listener {
	private String busterSuccessMessage;
	private String busterFailMessage;
	private ArrayList<Material> blacklistedBlocks;
	private HashMap<UUID, BukkitTask> chunkRemovalRunnables = new HashMap<>();
	
	public ChunkBuster(String busterSuccessMessage, String busterFailMessage, ArrayList<Material> blacklistedBlocks) {
		super();
		this.busterSuccessMessage = busterSuccessMessage;
		this.busterFailMessage = busterFailMessage;
		this.blacklistedBlocks = blacklistedBlocks;
		blacklistedBlocks.add(Material.AIR);
	}

	@EventHandler
	public void useChunkBuster (PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getItem() != null) {
				if (e.getItem().getType() == CmdChunkBuster.busterItemType) {
					if (e.getItem().hasItemMeta()) {
						if (e.getItem().getItemMeta().hasLore()) {
							if (ItemDataUtil.hasHiddenString(e.getItem().getItemMeta().getLore().get(0))) {
								if (ItemDataUtil.extractHiddenString(e.getItem().getItemMeta().getLore().get(0)).equals("Chunkbuster")) {
									e.setCancelled(true);
									if (Board.getInstance().getFactionAt(new FLocation(e.getClickedBlock().getLocation())).getTag().equals(FPlayers.getInstance().getByPlayer(e.getPlayer()).getFaction().getTag())) {
										if (!chunkRemovalRunnables.containsKey(e.getPlayer().getUniqueId())) {
											Chunk c = e.getClickedBlock().getChunk();
											BukkitTask taskID = Bukkit.getScheduler().runTaskTimer(FAIOPlugin.getInstance(), new Runnable() {
												int yLevel = e.getClickedBlock().getY() + 10;
												@Override
												public void run() {
													if (yLevel == 1) {
														chunkRemovalRunnables.get(e.getPlayer().getUniqueId()).cancel();
														chunkRemovalRunnables.remove(e.getPlayer().getUniqueId());
													}
													for (int x = 0; x < 16; x++) {
														for (int z = 0; z < 16; z++) {
															if (!blacklistedBlocks.contains(c.getBlock(x, yLevel, z).getType())) {
																c.getBlock(x, yLevel, z).setType(Material.AIR);
															}
															c.getWorld().playEffect(c.getBlock(x, yLevel, z).getLocation(), Effect.MOBSPAWNER_FLAMES, 32);
														}
													}
													yLevel--;
												}
											}, 0, 5);
											chunkRemovalRunnables.put(e.getPlayer().getUniqueId(), taskID);
											e.getPlayer().sendMessage(Utils.cc(busterSuccessMessage));
											if (e.getPlayer().getItemInHand().getAmount() > 1) {
												e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand().getAmount() - 1);
											}
											else {
												e.getPlayer().getInventory().clear(e.getPlayer().getInventory().getHeldItemSlot());
											}
										}
										else {
											e.getPlayer().sendMessage(Utils.cc("&4&l(!) You can only use 1 chunkbuster at a time! Wait until the previous one has finished"));
										}
									}
									else {
										e.getPlayer().sendMessage(Utils.cc(busterFailMessage));
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
