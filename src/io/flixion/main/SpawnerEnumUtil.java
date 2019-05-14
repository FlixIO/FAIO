package io.flixion.main;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.WordUtils;

public enum SpawnerEnumUtil {
	BLAZE("blaze spawner"),
	CAVE_SPIDER("cave spider spawner"),
	CHICKEN("chicken spawner"),
	COW("cow spawner"),
	CREEPER("creeper spawner"),
	ENDER_DRAGON("enderdragon spawner"),
	ENDERMAN("enderman spawner"),
	ENDERMITE("endermite spawner"),
	GHAST("ghast spawner"),
	GIANT("giant zombie spawner"),
	MAGMA_CUBE("magmacube spawner"),
	MUSHROOM_COW("mooshroom spawner"),
	PIG("pig spawner"),
	PIG_ZOMBIE("zombie pigman spawner"),
	RABBIT("rabbit spawner"),
	SHEEP("sheep spawner"),
	SILVERFISH("silverfish spawner"),
	SKELETON("skeleton spawner"),
	SLIME("slime spawner"),
	SNOWMAN("snow golem spawner"),
	SPIDER("spider spawner"),
	SQUID("squid spawner"),
	VILLAGER("villager spawner"),
	WOLF("wolf spawner"),
	ZOMBIE("zombie spawner"),
	HORSE("horse spawner"),
	IRON_GOLEM("iron golem spawner"),
	OCELOT("ocelot spawner"),
	WITCH("witch spawner"),
	WITHER_SKELETON("wither skeleton spawner"),
	WITHER("wither spawner");
	
	private final String name;
	
	private SpawnerEnumUtil (String name) {
		this.name = name;
	}
	
	public String toString(){
        return name;
    }

    private static final Map<String, SpawnerEnumUtil> lookup = new HashMap<String, SpawnerEnumUtil>();
    //Returns the Material name from the given block name
    public static String getMaterialName(String fromEntityName){
        for(SpawnerEnumUtil n : values()){
            lookup.put(n.toString(), n);
        }   
        String result = lookup.get(fromEntityName).name();
        return result;
    }
   
    public String firstUpperCased(){
        char first = Character.toUpperCase(name.charAt(0));
        return first + name.substring(1);
    }
   
    public String firstAllUpperCased(){
        return WordUtils.capitalizeFully(name);
    }

    public String allUpperCased(){
        return name.toUpperCase();
    }
}
