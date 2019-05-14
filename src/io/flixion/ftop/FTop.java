package io.flixion.ftop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.LandUnclaimAllEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;

import io.flixion.main.FAIOPlugin;

public class FTop implements Listener {
	private ArrayList<Integer> chunkSync = new ArrayList<>();
	BukkitTask task;
	int mainTaskID;
	int claimedLandTaskID;
	private HashMap<String, FTopObjects> topFactionsUnordered = new HashMap<>();
	public static LinkedHashMap<String, FTopObjects> topFactionsOrdered = new LinkedHashMap<>();
	private HashMap<String, ArrayList<Chunk>> factionClaims = new HashMap<>();
	private HashMap<Material, Double> blockValues;
	private boolean includePlayerEco;
	private HashMap<EntityType, Double> spawnerValues;
	private HashMap<Enchantment, Double> enchantValues;

	public FTop(HashMap<Material, Double> blockValues, boolean includePlayerEco,
			HashMap<EntityType, Double> spawnerValues, HashMap<Enchantment, Double> enchantValues) {
		super();
		this.blockValues = blockValues;
		this.includePlayerEco = includePlayerEco;
		this.spawnerValues = spawnerValues;
		this.enchantValues = enchantValues;
	}

	public void initFactionClaimsMap() {
		for (Faction f : Factions.getInstance().getAllFactions()) {
			if (f.isNormal()) {
				ArrayList<Chunk> loc = new ArrayList<>();
				factionClaims.put(f.getTag(), loc);
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void checkChunk (ChunkLoadEvent e) {
		if (!Board.getInstance().getFactionAt(new FLocation(e.getChunk().getBlock(0, 0, 0).getLocation())).isWilderness()
				&& Board.getInstance().getFactionAt(new FLocation(e.getChunk().getBlock(0, 0, 0).getLocation())).isNormal()) {
			Faction f = Board.getInstance().getFactionAt(new FLocation(e.getChunk().getBlock(0, 0, 0).getLocation()));
			boolean contains = false;
			for (Chunk c : factionClaims.get(f.getTag())) {
				if (c.getX() == e.getChunk().getX() && c.getZ() == e.getChunk().getZ()) {
					contains = true;
				}
			}
			if (!contains) {
				factionClaims.get(f.getTag()).add(e.getChunk());
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void landClaim (LandClaimEvent e) {
		if (!e.getFaction().isSafeZone() && !e.getFaction().isWarZone()) {
			Faction f = e.getFaction();
			Chunk cNew = e.getLocation().getWorld().getChunkAt((int) e.getLocation().getX(), (int) e.getLocation().getZ());
			boolean contains = false;
			for (Chunk c : factionClaims.get(f.getTag())) {
				if (c.getX() == cNew.getX() && c.getZ() == cNew.getZ()) {
					contains = true;
				}
			}
			if (!contains) {
				factionClaims.get(f.getTag()).add(cNew);
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void landUnclaim (LandUnclaimEvent e) {
		if (!e.getFaction().isSafeZone() && !e.getFaction().isWarZone()) {
			if (!Board.getInstance().getFactionAt(e.getLocation()).isWilderness()
					&& Board.getInstance().getFactionAt(e.getLocation()).isNormal()) {
				Faction f = e.getFaction();
				Chunk cNew = e.getLocation().getWorld().getChunkAt((int) e.getLocation().getX(), (int) e.getLocation().getZ());
				boolean contains = false;
				int index = 0;
				for (Chunk c : factionClaims.get(f.getTag())) {
					if (c.getX() == cNew.getX() && c.getZ() == cNew.getZ()) {
						contains = true;
					}
					else {
						index++;
					}
				}
				if (contains) {
					factionClaims.get(f.getTag()).remove(index);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void landUnclaimAll (LandUnclaimAllEvent e) {
		Faction f = e.getFaction();
		if (factionClaims.containsKey(f.getTag())) {
			ArrayList<Chunk> loc = new ArrayList<>();
			factionClaims.replace(f.getTag(), loc);
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void createNewFaction (FactionCreateEvent e) {
		if (!factionClaims.containsKey(e.getFactionTag())) {
			ArrayList<Chunk> loc = new ArrayList<>();
			factionClaims.put(e.getFactionTag(), loc);
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void disbandFaction (FactionDisbandEvent e) {
		if (factionClaims.containsKey(e.getFaction().getTag())) {
			factionClaims.remove(e.getFaction().getTag());
		}
	}

	@SuppressWarnings("deprecation")
	public void calculateChunkWorth() {
		topFactionsUnordered.clear();
		topFactionsOrdered.clear();
		HashMap<String, ArrayList<Long>> fTop = new HashMap<>();
		HashMap<String, HashMap<String, Integer>> fTopSpawners = new HashMap<>();
		for (Map.Entry<String, ArrayList<Chunk>> e : factionClaims.entrySet()) {
			ArrayList<Long> worth = new ArrayList<>();
			worth.add(0L);
			worth.add(0L);
			HashMap<String, Integer> spawnerCount = new HashMap<>();
			fTopSpawners.put(e.getKey(), spawnerCount);
			fTop.put(e.getKey(), worth);
			if (includePlayerEco) {
				for (FPlayer fp : Factions.getInstance().getByTag(e.getKey()).getFPlayers()) {
					fTop.get(e.getKey()).set(0, (long) (worth.get(0) + FAIOPlugin.getEco().getBalance(fp.getName())));
					fTop.get(e.getKey()).set(1, (long) (worth.get(1) + FAIOPlugin.getEco().getBalance(fp.getName())));
				}
			}
			for (Map.Entry<EntityType, Double> entry : spawnerValues.entrySet()) {
				fTopSpawners.get(e.getKey()).put(entry.getKey().toString(), 0);
			}
			int tickIncrease = 0;
			for (int i = 0; i < e.getValue().size(); i++) {
				Chunk c = e.getValue().get(i);
				task = Bukkit.getScheduler().runTaskLater(FAIOPlugin.getInstance(), new Runnable() {

					@Override
					public void run() {
						for (int x = 0; x < 16; x++) {
							for (int y = 1; y < 257; y++) {
								for (int z = 0; z < 16; z++) {
									BlockState bs = c.getBlock(x, y, z).getState();
									if (bs instanceof Chest) {
										Chest chest = (Chest) bs;
										for (ItemStack i : chest.getInventory()) {
											if (i != null) {
												if (i.getType() == Material.MOB_SPAWNER) {
													BlockStateMeta bsm = (BlockStateMeta) i.getItemMeta();
													CreatureSpawner spawner = (CreatureSpawner) bsm.getBlockState();
													if (spawnerValues.containsKey(spawner.getSpawnedType())) {
														fTop.get(e.getKey()).set(0,
																(long) (worth.get(0)
																		+ spawnerValues.get(spawner.getSpawnedType())
																				* i.getAmount()));
														fTopSpawners.get(e.getKey()).replace(
																spawner.getSpawnedType().toString(),
																fTopSpawners.get(e.getKey())
																		.get(spawner.getSpawnedType().toString())
																		+ i.getAmount());
													}
												} else {
													for (Map.Entry<Enchantment, Integer> entry : i.getEnchantments()
															.entrySet()) {
														if (enchantValues.containsKey(entry.getKey())
																&& entry.getValue() == entry.getKey().getMaxLevel()) {
															fTop.get(e.getKey()).set(0,
																	(long) (worth.get(0) + enchantValues.get(entry.getKey())));
														}
													}
												}
											}
										}
									} else if (bs instanceof CreatureSpawner) {
										CreatureSpawner spawner = (CreatureSpawner) bs;
										if (spawnerValues.containsKey(spawner.getSpawnedType())) {
											fTop.get(e.getKey()).set(0,
													(long) (worth.get(0) + spawnerValues.get(spawner.getSpawnedType())));
											fTopSpawners.get(e.getKey()).replace(spawner.getSpawnedType().toString(),
													fTopSpawners.get(e.getKey())
															.get(spawner.getSpawnedType().toString()) + 1);
										}
									} else {
										if (blockValues.containsKey(bs.getBlock().getType())) {
											fTop.get(e.getKey()).set(0,
													(long) (worth.get(0) + blockValues.get(bs.getBlock().getType())));
										}
									}
								}
							}
						}
						chunkSync.remove(chunkSync.size() - 1);
					}
				}, 0 + tickIncrease);
				tickIncrease += 2;
				chunkSync.add(task.getTaskId());
			}
		}
		mainTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(FAIOPlugin.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				if (chunkSync.size() == 0) {
					for (Map.Entry<String, ArrayList<Long>> entry : fTop.entrySet()) {
						FTopObjects ftop = new FTopObjects(entry.getValue().get(0), entry.getKey(), entry.getValue().get(1));
						ftop.setSpawnerCount(fTopSpawners.get(entry.getKey()));
						topFactionsUnordered.put(entry.getKey(), ftop);
					}
					int count = topFactionsUnordered.size();
					for (int j = 0; j < count; j++) {
						FTopObjects highest = new FTopObjects(0, "Order", 0);
						for (Map.Entry<String, FTopObjects> entry2 : topFactionsUnordered.entrySet()) {
							if (entry2.getValue().getValue() >= highest.getValue()) {
								highest = entry2.getValue();
							}
						}
						topFactionsUnordered.remove(highest.getFactionTag());
						topFactionsOrdered.put(highest.getFactionTag(), highest);
					}
					Bukkit.getScheduler().cancelTask(mainTaskID);
				}
			}
		}, 0, 20);
	}
}
