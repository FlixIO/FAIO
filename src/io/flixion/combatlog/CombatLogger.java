package io.flixion.combatlog;

import java.util.UUID;

import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class CombatLogger {
	private UUID playerUUID;
	private UUID entityUUID;
	private ItemStack[] playerInventory;
	private ItemStack[] armorInventory;
	private double exp;
	private Villager v;
	private BukkitTask task;
	
	public CombatLogger(UUID playerUUID, UUID entityUUID, ItemStack[] playerInventory, ItemStack[] armorInventory,
			double exp, Villager v, BukkitTask task) {
		super();
		this.playerUUID = playerUUID;
		this.entityUUID = entityUUID;
		this.playerInventory = playerInventory;
		this.armorInventory = armorInventory;
		this.exp = exp;
		this.v = v;
		this.task = task;
	}
	
	public BukkitTask getTask() {
		return task;
	}
	public void setTask(BukkitTask task) {
		this.task = task;
	}
	public UUID getPlayerUUID() {
		return playerUUID;
	}
	public void setPlayerUUID(UUID playerUUID) {
		this.playerUUID = playerUUID;
	}
	public UUID getEntityUUID() {
		return entityUUID;
	}
	public void setEntityUUID(UUID entityUUID) {
		this.entityUUID = entityUUID;
	}
	public ItemStack[] getPlayerInventory() {
		return playerInventory;
	}
	public void setPlayerInventory(ItemStack[] playerInventory) {
		this.playerInventory = playerInventory;
	}
	public ItemStack[] getArmorInventory() {
		return armorInventory;
	}
	public void setArmorInventory(ItemStack[] armorInventory) {
		this.armorInventory = armorInventory;
	}
	public double getExp() {
		return exp;
	}
	public void setExp(double exp) {
		this.exp = exp;
	}
	public Villager getV() {
		return v;
	}
	public void setV(Villager v) {
		this.v = v;
	}
	
}
