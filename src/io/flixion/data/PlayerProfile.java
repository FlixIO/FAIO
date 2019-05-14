package io.flixion.data;

import java.util.HashMap;
import java.util.UUID;

public class PlayerProfile {
	private UUID uuid;
	private HashMap<String, Integer> crateKeys;
	private boolean fStealth = false;
	public PlayerProfile(UUID uuid, HashMap<String, Integer> crateKeys) {
		super();
		this.uuid = uuid;
		this.crateKeys = crateKeys;
	}
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public HashMap<String, Integer> getCrateKeys() {
		return crateKeys;
	}
	public void setCrateKeys(HashMap<String, Integer> crateKeys) {
		this.crateKeys = crateKeys;
	}
	public boolean isfStealth() {
		return fStealth;
	}
	public void setfStealth(boolean fStealth) {
		this.fStealth = fStealth;
	}
}
