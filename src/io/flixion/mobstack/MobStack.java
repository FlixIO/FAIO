package io.flixion.mobstack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import io.flixion.main.Utils;

public class MobStack implements Listener{
	private ArrayList<EntityType> stackableEntites = new ArrayList<>();
	private int stackRadius;
	private Random ran = new Random();
	private HashMap<Material, Integer> creeperDrops;
	private HashMap<Material, Integer> endermanDrops;
	private HashMap<Material, Integer> zombieDrops;
	private HashMap<Material, Integer> skeletonDrops;
	private HashMap<Material, Integer> blazeDrops;
	private HashMap<Material, Integer> cowDrops;
	private HashMap<Material, Integer> spiderDrops;
	private HashMap<Material, Integer> zombiePigDrops;
	private HashMap<Material, Integer> ironGolemDrops;
	private boolean ironGolemsSpawnOnFire;
	private int ironGolemHealth;

	public MobStack(ArrayList<EntityType> stackableEntites, int stackRadius, HashMap<Material, Integer> creeperDrops, HashMap<Material, Integer> endermanDrops,
			HashMap<Material, Integer> zombieDrops, HashMap<Material, Integer> skeletonDrops,
			HashMap<Material, Integer> blazeDrops, HashMap<Material, Integer> cowDrops,
			HashMap<Material, Integer> spiderDrops, HashMap<Material, Integer> zombiePigDrops,
			HashMap<Material, Integer> ironGolemDrops, boolean spawnOnFire, int igHealth) {
		super();
		this.stackableEntites = stackableEntites;
		this.stackRadius = stackRadius;
		this.creeperDrops = creeperDrops;
		this.endermanDrops = endermanDrops;
		this.zombieDrops = zombieDrops;
		this.skeletonDrops = skeletonDrops;
		this.blazeDrops = blazeDrops;
		this.cowDrops = cowDrops;
		this.spiderDrops = spiderDrops;
		this.zombiePigDrops = zombiePigDrops;
		this.ironGolemDrops = ironGolemDrops;
		this.ironGolemsSpawnOnFire = spawnOnFire;
		this.ironGolemHealth = igHealth;
	}

	@EventHandler (ignoreCancelled=true)
	public void spawnerStack (SpawnerSpawnEvent e) {
		if (e.getEntityType() == EntityType.IRON_GOLEM) {
			((IronGolem) e.getEntity()).setHealth(ironGolemHealth);
			if (ironGolemsSpawnOnFire) {
				e.getEntity().setFireTicks(100000);
			}
		}
		if (stackableEntites.contains(e.getEntity().getType())) {
			boolean nearbyStack = false;
			Entity stackEntity = null;
			for (Entity e1 : e.getEntity().getWorld().getNearbyEntities(e.getEntity().getLocation(), stackRadius, stackRadius, stackRadius)) {
				if (e1.getType() == e.getEntityType()) {
					nearbyStack = true;
					stackEntity = e1;
				}
			}
			if (nearbyStack) {
				if (stackEntity.getCustomName() == null) {
					stackEntity.setCustomName(Utils.cc("&e&lx2"));
					e.getEntity().remove();
				}
				else {
					int stackSize = Integer.parseInt(ChatColor.stripColor(stackEntity.getCustomName()).replace("x", ""));
					if (stackSize < 1000) {
						stackSize++;
						stackEntity.setCustomName(Utils.cc("&e&lx" + stackSize));
						e.getEntity().remove();
					} else {
						e.getEntity().setCustomName(Utils.cc("&e&lx1"));
					}
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled=true)
	public void stackDeath (EntityDeathEvent e) {
		if (stackableEntites.contains(e.getEntity().getType())) {
			if (!(e.getEntity() instanceof Player)) {
				if (e.getEntity().getCustomName() != null) {
					int stackSize = Integer.parseInt(ChatColor.stripColor(e.getEntity().getCustomName()).replace("x", ""));
					if (e.getEntity().getLastDamageCause().getCause() == DamageCause.FALL || e.getEntity().getLastDamageCause().getCause() == DamageCause.LAVA || e.getEntity().getLastDamageCause().getCause() == DamageCause.FIRE_TICK) {
						for (int i = 0; i < stackSize + 1; i++) {
							if (e.getEntityType() == EntityType.CREEPER) {
								generateItems(creeperDrops, e);
							} else if (e.getEntityType() == EntityType.ENDERMAN) {
								generateItems(endermanDrops, e);
							} else if (e.getEntityType() == EntityType.ZOMBIE) {
								generateItems(zombieDrops, e);
							} else if (e.getEntityType() == EntityType.SKELETON) {
								generateItems(skeletonDrops, e);
							} else if (e.getEntityType() == EntityType.BLAZE) {
								generateItems(blazeDrops, e);
							} else if (e.getEntityType() == EntityType.COW) {
								generateItems(cowDrops, e);
							} else if (e.getEntityType() == EntityType.SPIDER) {
								generateItems(spiderDrops, e);
							} else if (e.getEntityType() == EntityType.PIG_ZOMBIE) {
								generateItems(zombiePigDrops, e);
							} else if (e.getEntityType() == EntityType.IRON_GOLEM) {
								generateItems(ironGolemDrops, e);
							}
							for (ItemStack item : e.getDrops()) {
								e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack (item.getType(), item.getAmount()));
							}
						}
						e.setDroppedExp(e.getDroppedExp() * stackSize);
					}
					else {
						if (stackSize > 1) {
							LivingEntity newStack = (LivingEntity) e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(), e.getEntityType());
							newStack.setCustomName(Utils.cc("&e&lx" + (stackSize - 1)));
							if (e.getEntityType() == EntityType.CREEPER) {
								generateItems(creeperDrops, e);
							} else if (e.getEntityType() == EntityType.ENDERMAN) {
								generateItems(endermanDrops, e);
							} else if (e.getEntityType() == EntityType.ZOMBIE) {
								generateItems(zombieDrops, e);
							} else if (e.getEntityType() == EntityType.SKELETON) {
								generateItems(skeletonDrops, e);
							} else if (e.getEntityType() == EntityType.BLAZE) {
								generateItems(blazeDrops, e);
							} else if (e.getEntityType() == EntityType.COW) {
								generateItems(cowDrops, e);
							} else if (e.getEntityType() == EntityType.SPIDER) {
								generateItems(spiderDrops, e);
							} else if (e.getEntityType() == EntityType.PIG_ZOMBIE) {
								generateItems(zombiePigDrops, e);
							} else if (e.getEntityType() == EntityType.IRON_GOLEM) {
								generateItems(ironGolemDrops, e);
							}
							for (ItemStack item : e.getDrops()) {
								e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack (item.getType(), item.getAmount()));
							}
						}
					}
				}
			}
		}
		else {
			if (e.getEntityType() == EntityType.CREEPER) {
				generateItems(creeperDrops, e);
			} else if (e.getEntityType() == EntityType.ENDERMAN) {
				generateItems(endermanDrops, e);
			} else if (e.getEntityType() == EntityType.ZOMBIE) {
				generateItems(zombieDrops, e);
			} else if (e.getEntityType() == EntityType.SKELETON) {
				generateItems(skeletonDrops, e);
			} else if (e.getEntityType() == EntityType.BLAZE) {
				generateItems(blazeDrops, e);
			} else if (e.getEntityType() == EntityType.COW) {
				generateItems(cowDrops, e);
			} else if (e.getEntityType() == EntityType.SPIDER) {
				generateItems(spiderDrops, e);
			} else if (e.getEntityType() == EntityType.PIG_ZOMBIE) {
				generateItems(zombiePigDrops, e);
			} else if (e.getEntityType() == EntityType.IRON_GOLEM) {
				generateItems(ironGolemDrops, e);
			}
		}
	}
	
	public void generateItems(HashMap<Material, Integer> mob, EntityDeathEvent e) {
		e.getDrops().clear();
		for (Map.Entry<Material, Integer> entry : mob.entrySet()) {
			if (entry.getValue() < 100) {
				if (ran.nextInt(101) <= entry.getValue()) {
					e.getDrops().add(new ItemStack(entry.getKey(), 1));
				}
			} else if (entry.getValue() >= 100) {
				e.getDrops().add(new ItemStack(entry.getKey(), (int) entry.getValue() / 100));
			}
			if (e.getEntity().getKiller() instanceof Player) {
				if (((Player) e.getEntity().getKiller()).getItemInHand().hasItemMeta()) {
					if (((Player) e.getEntity().getKiller()).getItemInHand().getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_MOBS)) {
						int level = ((Player) e.getEntity().getKiller()).getItemInHand().getItemMeta().getEnchants().get(Enchantment.LOOT_BONUS_MOBS);
						for (ItemStack i : e.getDrops()) {
							if (ran.nextInt(101) <= 15) {
								i.setAmount(i.getAmount() * level);
							}
						}
					}
				}
			}
		}
	}
}
