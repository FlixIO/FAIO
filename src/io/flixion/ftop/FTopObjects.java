package io.flixion.ftop;

import java.util.HashMap;

public class FTopObjects {
	private long value;
	private long playerWealthValue;
	private String factionTag;
	private HashMap<String, Integer> spawnerCount = new HashMap<>();
	
	public FTopObjects(long value, String factionTag, long playerWealth) {
		super();
		this.value = value;
		this.factionTag = factionTag;
		this.playerWealthValue = playerWealth;
	}

	public double getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public String getFactionTag() {
		return factionTag;
	}

	public void setFactionTag(String factionTag) {
		this.factionTag = factionTag;
	}

	public HashMap<String, Integer> getSpawnerCount() {
		return spawnerCount;
	}

	public void setSpawnerCount(HashMap<String, Integer> spawnerCount) {
		this.spawnerCount = spawnerCount;
	}

	public long getPlayerWealthValue() {
		return playerWealthValue;
	}

	public void setPlayerWealthValue(long playerWealthValue) {
		this.playerWealthValue = playerWealthValue;
	}
	
	
}
