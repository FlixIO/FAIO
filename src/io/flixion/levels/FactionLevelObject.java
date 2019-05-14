package io.flixion.levels;

import java.util.HashMap;

public class FactionLevelObject {
	private double EXP;
	private String factionID;
	private HashMap<String, Integer> generatingPlayerTaskIDs;
	private int level;
	
	public FactionLevelObject(double eXP, String factionID, HashMap<String, Integer> generatingPlayerTaskIDs, int level) {
		super();
		this.EXP = eXP;
		this.factionID = factionID;
		this.generatingPlayerTaskIDs = generatingPlayerTaskIDs;
		this.level = level;	
	}

	public double getEXP() {
		return EXP;
	}

	public void setEXP(double eXP) {
		EXP = eXP;
	}

	public String getFactionID() {
		return factionID;
	}

	public void setFactionID(String factionID) {
		this.factionID = factionID;
	}

	public HashMap<String, Integer> getGeneratingPlayerTaskIDs() {
		return generatingPlayerTaskIDs;
	}

	public void setGeneratingPlayerTaskIDs(HashMap<String, Integer> generatingPlayerTaskIDs) {
		this.generatingPlayerTaskIDs = generatingPlayerTaskIDs;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
