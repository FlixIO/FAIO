package io.flixion.main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.zaxxer.hikari.HikariDataSource;

import io.flixion.chunkbuster.ChunkBuster;
import io.flixion.chunkbuster.CmdChunkBuster;
import io.flixion.combatlog.CombatLogger;
import io.flixion.combatlog.LogHandler;
import io.flixion.crates.CmdCrate;
import io.flixion.crates.CrateObject;
import io.flixion.crates.CrateSQL;
import io.flixion.crates.Crates;
import io.flixion.data.PlayerHandler;
import io.flixion.factionsfly.Fly;
import io.flixion.ftop.CmdFTop;
import io.flixion.ftop.CmdRefreshFTop;
import io.flixion.ftop.FTop;
import io.flixion.genbucket.GenBucket;
import io.flixion.hopper.CropHopper;
import io.flixion.levels.CmdLevel;
import io.flixion.levels.CmdSetLevel;
import io.flixion.levels.LevelEffect;
import io.flixion.levels.LevelSQL;
import io.flixion.levels.Levels;
import io.flixion.main.EntityHider.Policy;
import io.flixion.misc.AutoBroadcaster;
import io.flixion.misc.CmdWithdraw;
import io.flixion.misc.Cooldowns;
import io.flixion.misc.CustomMessages;
import io.flixion.misc.EmptyBottleRemover;
import io.flixion.misc.JellyLegs;
import io.flixion.misc.NightVision;
import io.flixion.misc.PlayerManagement;
import io.flixion.misc.PreventCraft;
import io.flixion.misc.SilentTNT;
import io.flixion.misc.WaterInteractRedstone;
import io.flixion.misc.WebLimiter;
import io.flixion.misc.WorldborderPatches;
import io.flixion.mobstack.MobStack;
import io.flixion.scoreboard.EssentialsHookEvents;
import io.flixion.scoreboard.KothHookEvents;
import io.flixion.scoreboard.LuckPermsHook;
import io.flixion.scoreboard.PermissionsExHookEvents;
import io.flixion.scoreboard.ScoreboardHandler;
import io.flixion.sell.CmdSellchest;
import io.flixion.sell.CmdSellinv;
import io.flixion.shockwave.CmdShockwave;
import io.flixion.shockwave.ShockwaveTool;
import io.flixion.staffmode.CmdChatLock;
import io.flixion.staffmode.CmdClearChat;
import io.flixion.staffmode.CmdFreeze;
import io.flixion.staffmode.CmdStaffChat;
import io.flixion.staffmode.CmdStaffMode;
import io.flixion.staffmode.CmdUnfreeze;
import io.flixion.staffmode.StaffHandler;
import io.flixion.tntfill.CmdTNTFill;
import io.flixion.tntfill.CmdUnTNTFill;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import sun.net.www.content.text.plain;

@SuppressWarnings("deprecation")
public class FAIOPlugin extends JavaPlugin implements Listener {
	private static FAIOPlugin instance;
	private static Essentials essentialsPlugin;
	private static Economy econ;
	private static EntityHider entityHider;
	private static File crates;
	private static YamlConfiguration crateConfig;
	private YamlConfiguration settingsConfig;
	private static HikariDataSource ds = new HikariDataSource();
	public static ArrayList<String> crateNames;
	public static int ftopTaskID;
	public static boolean permissionsExFound = false;
	public static boolean scoreboardEnabled = false;
	public static boolean subsideKothHook = false;
	public static boolean essentialsHook = false;
	public static boolean luckPermsHook = false;
	public static String version = "1.4.4";
	public static boolean cratesImport = false;
	CommandMap cmdMap = null;

	public void onEnable() {
		if ((Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]).contains("v1_7")) {
			logConsoleError(
					"Detected a 1.7.x build or protocolhack, please use the appropriate version! This is for 1.8.x and above");
		}
		instance = this;
		entityHider = new EntityHider(this, Policy.BLACKLIST);
		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		try {
			final Field bukkitCmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCmdMap.setAccessible(true);
			cmdMap = (CommandMap) bukkitCmdMap.get(Bukkit.getServer());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		checkDependancies();
		saveSettingsFile();
		if (checkDatabaseConnection()) {
			if (importConfigData()) {
				checkCratesFile();
				importCrates();
				LevelSQL.initTblLevels();
			}
		}
	}

	public void onDisable() {
		for (Map.Entry<UUID, CombatLogger> entry : LogHandler.combatLoggers.entrySet()) {
			entry.getValue().getTask().cancel();
			entry.getValue().getV().remove();
		}
		for (Map.Entry<UUID, Map<String, BukkitTask>> entry : PlayerManagement.homeTaskList.entrySet()) {
			User u = FAIOPlugin.getEssentials().getUser(entry.getKey());
			for (String s : u.getHomes()) {
				try {
					if (Board.getInstance().getFactionAt(new FLocation(u.getHome(s))).isNormal()) {
						Faction f = Board.getInstance().getFactionAt(new FLocation(u.getHome(s)));
						if (f.getRelationTo(FPlayers.getInstance().getByPlayer(Bukkit.getPlayer(entry.getKey())).getFaction()).isEnemy()) {
							u.delHome(s);
						}
					}
				} catch (Exception e1) { // u.getHome(String) throws Exception??
					continue;
				}
			}
		}
	}

	public static EntityHider getEntityHider() {
		return entityHider;
	}

	public static HikariDataSource getHikariSource() {
		return ds;
	}

	public static FAIOPlugin getInstance() {
		return instance;
	}

	public static Economy getEco() {
		return econ;
	}

	public static File getCratesFile() {
		return crates;
	}

	public static YamlConfiguration getCratesConfig() {
		return crateConfig;
	}

	public void saveSettingsFile() {
		saveResource("config.yml", false);
		saveResource("settings.yml", false);
		settingsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "settings.yml"));
	}

	public boolean importConfigData() {
		boolean initSuccess = false;

		try {
			Bukkit.getServer().getPluginManager().registerEvents(new PlayerHandler(), this);
			if (settingsConfig.getBoolean("banknotes")) {
				CmdWithdraw cmdWithdraw = new CmdWithdraw("withdraw", getConfig().getString("banknote.noteName"),
						(ArrayList<String>) getConfig().getStringList("banknote.noteLore"),
						getConfig().getString("banknote.noteUseMessage"),
						getConfig().getString("banknote.withdrawFailMessage"),
						getConfig().getString("banknote.withdrawSuccessMessage"),
						Material.valueOf(getConfig().getString("banknote.noteItemType")));
				cmdMap.register("withdraw", cmdWithdraw);
				Bukkit.getServer().getPluginManager().registerEvents(cmdWithdraw, this);
			}
			if (settingsConfig.getBoolean("chunkBusters")) {
				ArrayList<String> lore = new ArrayList<>();
				for (String s : getConfig().getStringList("chunkbuster.busterLore")) {
					lore.add(s);
				}
				CmdChunkBuster cmdChunkbuster = new CmdChunkBuster("chunkbuster",
						getConfig().getString("chunkbuster.busterName"), lore,
						Material.valueOf(getConfig().getString("chunkbuster.busterItemType")),
						getConfig().getString("chunkbuster.busterTargetCommandMessage"),
						getConfig().getString("chunkbuster.busterSenderCommandMessage"));
				ArrayList<Material> blacklistedBlocks = new ArrayList<>();
				for (String s : getConfig().getStringList("chunkbuster.blacklistedBlocks")) {
					blacklistedBlocks.add(Material.valueOf(s));
				}
				ChunkBuster chunkBuster = new ChunkBuster(getConfig().getString("chunkbuster.busterSuccessMessage"),
						getConfig().getString("chunkbuster.busterFailMessage"), blacklistedBlocks);
				Bukkit.getServer().getPluginManager().registerEvents(chunkBuster, this);
				cmdMap.register("chunkbuster", cmdChunkbuster);

			}
			if (settingsConfig.getBoolean("genbuckets")) {
				GenBucket genBucket = new GenBucket(getConfig().getInt("genbuckets.blocksPerSecond"),
						(ArrayList<String>) getConfig().getStringList("genbuckets.genBucketLore"),
						getConfig().getString("genbuckets.genBucketName"),
						getConfig().getInt("genbuckets.genBucketItemID"),
						getConfig().getDouble("genbuckets.cobblestoneCost"),
						getConfig().getDouble("genbuckets.obsidianCost"), getConfig().getDouble("genbuckets.sandCost"),
						"genbucket");
				Bukkit.getServer().getPluginManager().registerEvents(genBucket, this);
				cmdMap.register("genbucket", genBucket);
			}
			if (settingsConfig.getBoolean("sellchest")) {
				HashMap<String, Double> sellableItems = new HashMap<>();
				for (String s : getConfig().getStringList("sellchest.sellableItems")) {
					sellableItems.put(s.split("#")[0], Double.parseDouble(s.split("#")[1]));
				}
				CmdSellchest sellChest = new CmdSellchest(getConfig().getInt("sellchest.radius"), sellableItems,
						getConfig().getString("sellchest.sellText"), "sellchest");
				CmdSellinv sellInv = new CmdSellinv(getConfig().getInt("sellchest.radius"), sellableItems,
						getConfig().getString("sellchest.sellText"), "sellinv");
				cmdMap.register("sellchest", sellChest);
				cmdMap.register("sellinv", sellInv);
			}
			if (settingsConfig.getBoolean("tntfill")) {
				CmdTNTFill tntFill = new CmdTNTFill(getConfig().getInt("tntfill.radius"),
						getConfig().getString("tntfill.successFillText"), getConfig().getString("tntfill.failFillText"),
						getConfig().getString("tntfill.noNearbyDispensersText"), "tntfill");
				cmdMap.register("tntfill", tntFill);
			}
			if (settingsConfig.getBoolean("untntfill")) {
				CmdUnTNTFill untntFill = new CmdUnTNTFill(getConfig().getInt("untntfill.radius"),
						getConfig().getString("untntfill.unFillText"),
						getConfig().getString("untntfill.noNearbyDispensersUnfillText"),
						getConfig().getString("untntfill.noInventorySpaceText"), "untntfill");
				cmdMap.register("untntfill", untntFill);
			}
			if (settingsConfig.getBoolean("crates")) {
				Crates crate = new Crates(getConfig().getString("crates.insufficientKeysText"),
						getConfig().getString("crates.winItemText"));

				CmdCrate crateCmd = new CmdCrate(getConfig().getString("crates.hologramTitleFormat"),
						getConfig().getString("crates.createCrateSuccessText"),
						getConfig().getString("crates.giveKeyTargetText"),
						getConfig().getString("crates.giveKeySenderText"), "crate");
				Bukkit.getServer().getPluginManager().registerEvents(crate, this);
				cmdMap.register("crate", crateCmd);
				cratesImport = true;
			}
			if (settingsConfig.getBoolean("mobstacking")) {
				ArrayList<EntityType> stackableEntities = new ArrayList<>();
				for (String s : getConfig().getStringList("mobstacking.stackableEntities")) {
					stackableEntities.add(EntityType.valueOf(s));
				}
				HashMap<Material, Integer> creeperDrops = new HashMap<>();
				for (String s : getConfig().getStringList("mobdrops.creeperDrops")) {
					String[] info = s.split("#");
					creeperDrops.put(Material.valueOf(info[0]), Integer.parseInt(info[1]));
				}
				HashMap<Material, Integer> endermanDrops = new HashMap<>();
				for (String s : getConfig().getStringList("mobdrops.endermanDrops")) {
					String[] info = s.split("#");
					endermanDrops.put(Material.valueOf(info[0]), Integer.parseInt(info[1]));
				}
				HashMap<Material, Integer> zombieDrops = new HashMap<>();
				for (String s : getConfig().getStringList("mobdrops.zombieDrops")) {
					String[] info = s.split("#");
					zombieDrops.put(Material.valueOf(info[0]), Integer.parseInt(info[1]));
				}
				HashMap<Material, Integer> skeletonDrops = new HashMap<>();
				for (String s : getConfig().getStringList("mobdrops.skeletonDrops")) {
					String[] info = s.split("#");
					skeletonDrops.put(Material.valueOf(info[0]), Integer.parseInt(info[1]));
				}
				HashMap<Material, Integer> blazeDrops = new HashMap<>();
				for (String s : getConfig().getStringList("mobdrops.blazeDrops")) {
					String[] info = s.split("#");
					blazeDrops.put(Material.valueOf(info[0]), Integer.parseInt(info[1]));
				}
				HashMap<Material, Integer> cowDrops = new HashMap<>();
				for (String s : getConfig().getStringList("mobdrops.cowDrops")) {
					String[] info = s.split("#");
					cowDrops.put(Material.valueOf(info[0]), Integer.parseInt(info[1]));
				}
				HashMap<Material, Integer> spiderDrops = new HashMap<>();
				for (String s : getConfig().getStringList("mobdrops.spiderDrops")) {
					String[] info = s.split("#");
					spiderDrops.put(Material.valueOf(info[0]), Integer.parseInt(info[1]));
				}
				HashMap<Material, Integer> zombiePigDrops = new HashMap<>();
				for (String s : getConfig().getStringList("mobdrops.zombiePigDrops")) {
					String[] info = s.split("#");
					zombiePigDrops.put(Material.valueOf(info[0]), Integer.parseInt(info[1]));
				}
				HashMap<Material, Integer> ironGolemDrops = new HashMap<>();
				for (String s : getConfig().getStringList("mobdrops.ironGolemDrops")) {
					String[] info = s.split("#");
					ironGolemDrops.put(Material.valueOf(info[0]), Integer.parseInt(info[1]));
				}
				MobStack mobStacking = new MobStack(stackableEntities, getConfig().getInt("mobstacking.stackRadius"),
						creeperDrops, endermanDrops, zombieDrops, skeletonDrops, blazeDrops, cowDrops, spiderDrops,
						zombiePigDrops, ironGolemDrops, getConfig().getBoolean("misc.ironGolemsSpawnOnFire"),
						getConfig().getInt("misc.ironGolemHealth"));
				Bukkit.getServer().getPluginManager().registerEvents(mobStacking, this);
			}
			if (settingsConfig.getBoolean("levelSystem")) {
				HashMap<Integer, Double> upgradeCosts = new HashMap<>();
				for (String s : getConfig().getStringList("flevel.upgradeCosts")) {
					upgradeCosts.put(Integer.parseInt(s.split("#")[0]), Double.parseDouble(s.split("#")[1]));
				}
				Levels levels = new Levels(getConfig().getDouble("flevel.expPerMinute"), upgradeCosts,
						getConfig().getString("flevel.broadcastFactionUpgradeNotification"));
				CmdLevel fLevelGUI = new CmdLevel(getConfig().getString("flevel.notInAFactionText"));
				Bukkit.getServer().getPluginManager().registerEvents(levels, this);
				Bukkit.getServer().getPluginManager().registerEvents(new LevelEffect(), this);
				Bukkit.getServer().getPluginManager().registerEvents(fLevelGUI, this);
				cmdMap.register("fsetlevel", new CmdSetLevel("fsetlevel"));
			}
			if (settingsConfig.getBoolean("ftop")) {
				HashMap<EntityType, Double> spawnerValues = new HashMap<>();
				for (String s : getConfig().getStringList("ftop.spawnerValues")) {
					spawnerValues.put(EntityType.valueOf(s.split("#")[0]), Double.parseDouble(s.split("#")[1]));

				}
				HashMap<Material, Double> blockValues = new HashMap<>();
				for (String s : getConfig().getStringList("ftop.blockValues")) {
					blockValues.put(Material.valueOf(s.split("#")[0]), Double.parseDouble(s.split("#")[1]));

				}
				HashMap<Enchantment, Double> enchantValues = new HashMap<>();
				for (String s : getConfig().getStringList("ftop.spawnerValues")) {
					enchantValues.put(Enchantment.getByName(s.split("#")[0]), Double.parseDouble(s.split("#")[1]));

				}
				FTop ftop = new FTop(blockValues, getConfig().getBoolean("ftop.includePlayerWealth"), spawnerValues,
						enchantValues);
				CmdRefreshFTop refreshFtop = new CmdRefreshFTop(ftop);
				ftop.initFactionClaimsMap();
				initFtop(ftop, true);
				Bukkit.getServer().getPluginManager()
						.registerEvents(new CmdFTop(getConfig().getString("ftop.displayInformationFormat")), this);
				Bukkit.getServer().getPluginManager().registerEvents(ftop, this);
				Bukkit.getServer().getPluginManager().registerEvents(refreshFtop, this);
			}
			if (settingsConfig.getBoolean("shockwaves")) {
				HashMap<Enchantment, Integer> defaultEnchantments = new HashMap<>();
				ArrayList<String> shockwaveLore = new ArrayList<>();
				shockwaveLore.add("");
				for (String s : getConfig().getStringList("shockwave.shockwaveDefaultEnchantments")) {
					if (Enchantment.getByName(s.split("#")[0]) != null) {
						if (Integer.parseInt(s.split("#")[1]) <= Enchantment.getByName(s.split("#")[0]).getMaxLevel()) {
							int level = Integer.parseInt(s.split("#")[1]);
							Enchantment e = Enchantment.getByName(s.split("#")[0]);
							defaultEnchantments.put(e, level);
						}
					}
				}
				for (String s : getConfig().getStringList("shockwave.shockwaveLore")) {
					shockwaveLore.add(Utils.cc(s));
				}
				CmdShockwave shockwave = new CmdShockwave(getConfig().getString("shockwave.shockwaveName"),
						shockwaveLore, defaultEnchantments, "shockwave");
				Bukkit.getServer().getPluginManager().registerEvents(new ShockwaveTool(), this);
				cmdMap.register("shockwave", shockwave);
			}
			if (settingsConfig.getBoolean("combatlog")) {
				ArrayList<String> blockedCommands = new ArrayList<>();
				for (String s : getConfig().getStringList("combatlog.blockedCommands")) {
					blockedCommands.add(s.toLowerCase());
				}
				LogHandler combatLog = new LogHandler(getConfig().getString("combatlog.loggerNameFormat"),
						getConfig().getInt("combatlog.tagTime"), blockedCommands);
				Bukkit.getServer().getPluginManager().registerEvents(combatLog, this);
			}
			if (settingsConfig.getBoolean("factionsFly")) {
				Fly factionsFly = new Fly(getConfig().getInt("factionsfly.disableWhenNearbyEnemyBlocks"),
						getConfig().getString("factionsfly.nearbyEnemyWhileFlyingText"),
						getConfig().getString("factionsfly.leaveOwnTerritoryWhileFlyingText"),
						getConfig().getString("factionsfly.inCombatWhileFlyingText"));
				Bukkit.getServer().getPluginManager().registerEvents(factionsFly, this);
			}
			if (settingsConfig.getBoolean("broadcaster")) {
				ArrayList<String> messagesToBroadcast = (ArrayList<String>) getConfig()
						.getStringList("broadcaster.messagesToBroadcast");

				new AutoBroadcaster(messagesToBroadcast, getConfig().getString("broadcaster.broadcastHeader"),
						getConfig().getString("broadcaster.broadcastFooter"),
						getConfig().getString("broadcaster.broadcasterPrefix"),
						getConfig().getInt("broadcaster.broadcastInterval"));
			}
			if (settingsConfig.getBoolean("cooldowns")) {
				Cooldowns cooldowns = new Cooldowns(getConfig().getInt("cooldowns.enderpearlThrow"),
						getConfig().getInt("cooldowns.regularGoldenApple"),
						getConfig().getInt("cooldowns.superGoldenApple"));
				Bukkit.getServer().getPluginManager().registerEvents(cooldowns, this);
			}
			if (settingsConfig.getBoolean("nocraft")) {
				ArrayList<Material> uncraftableItems = new ArrayList<>();
				for (String s : getConfig().getStringList("nocraft.items")) {
					uncraftableItems.add(Material.valueOf(s));
				}
				PreventCraft preventCrafting = new PreventCraft(uncraftableItems,
						getConfig().getString("nocraft.cannotCraftItemText"));
				Bukkit.getServer().getPluginManager().registerEvents(preventCrafting, this);
			}
			if (settingsConfig.getBoolean("nightvision")) {
				NightVision nv = new NightVision(getConfig().getString("nightvision.enabledText"),
						getConfig().getString("nightvision.disabledText"), "nv");
				cmdMap.register("nv", nv);
			}
			if (settingsConfig.getBoolean("staffmode")) {
				ArrayList<String> freezeItemLore = new ArrayList<>();
				freezeItemLore.add(ItemDataUtil.encodeString("Staff#Freeze"));
				for (String s : getConfig().getStringList("staffmode.freezeItemLore")) {
					freezeItemLore.add(Utils.cc(s));
				}
				ArrayList<String> randomTeleportItemLore = new ArrayList<>();
				randomTeleportItemLore.add(ItemDataUtil.encodeString("Staff#RandomTP"));
				for (String s : getConfig().getStringList("staffmode.randomTeleportItemLore")) {
					randomTeleportItemLore.add(Utils.cc(s));
				}
				ArrayList<String> leaveStaffModeLore = new ArrayList<>();
				leaveStaffModeLore.add(ItemDataUtil.encodeString("Staff#LeaveStaff"));
				for (String s : getConfig().getStringList("staffmode.leaveStaffModeItemLore")) {
					leaveStaffModeLore.add(Utils.cc(s));
				}
				ArrayList<String> knockbackItemLore = new ArrayList<>();
				knockbackItemLore.add(ItemDataUtil.encodeString("Staff#Knockback"));
				for (String s : getConfig().getStringList("staffmode.knockbackItemLore")) {
					knockbackItemLore.add(Utils.cc(s));
				}
				ArrayList<String> cpsCheckerItemLore = new ArrayList<>();
				cpsCheckerItemLore.add(ItemDataUtil.encodeString("Staff#CPSCheck"));
				for (String s : getConfig().getStringList("staffmode.cpsCheckerItemLore")) {
					cpsCheckerItemLore.add(Utils.cc(s));
				}
				CmdStaffMode cmdStaffMode = new CmdStaffMode(
						Material.valueOf(getConfig().getString("staffmode.freezeItemEnum")),
						Material.valueOf(getConfig().getString("staffmode.randomTeleportItemEnum")),
						Material.valueOf(getConfig().getString("staffmode.knockbackItemEnum")),
						Material.valueOf(getConfig().getString("staffmode.cpsCheckerItemEnum")),
						Material.valueOf(getConfig().getString("staffmode.leaveStaffModeEnum")), freezeItemLore,
						randomTeleportItemLore, knockbackItemLore, cpsCheckerItemLore, leaveStaffModeLore,
						Utils.cc(getConfig().getString("staffmode.freezeItemName")),
						Utils.cc(getConfig().getString("staffmode.randomTeleportItemName")),
						Utils.cc(getConfig().getString("staffmode.knockbackItemName")),
						Utils.cc(getConfig().getString("staffmode.cpsCheckerItemName")),
						Utils.cc(getConfig().getString("staffmode.leaveStaffModeItemName")),
						Utils.cc(getConfig().getString("staffmode.staffModeEnabledText")),
						Utils.cc(getConfig().getString("staffmode.staffModeDisabledText")),
						Utils.cc(getConfig().getString("staffmode.staffEnteredStaffModeNotification")),
						Utils.cc(getConfig().getString("staffmode.staffLeftStaffModeNotification")), "staff");
				ArrayList<String> frozenInventoryItemLore = new ArrayList<String>();
				for (String s : getConfig().getStringList("staffmode.frozenInventoryItemLore")) {
					frozenInventoryItemLore.add(Utils.cc(s));
				}
				StaffHandler staffHandler = new StaffHandler(
						getConfig().getString("staffmode.frozenInventoryItemMessage"), frozenInventoryItemLore);
				CmdChatLock lockChat = new CmdChatLock(getConfig().getString("staffmode.chatLockBroadcastMessage"),
						getConfig().getString("staffmode.chatUnlockBroadcastMessage"), "lockchat");
				CmdClearChat clearChat = new CmdClearChat(getConfig().getString("staffmode.chatClearBroadcastMessage"),
						"clearchat");
				Bukkit.getServer().getPluginManager().registerEvents(staffHandler, this);
				cmdMap.register("staff", cmdStaffMode);
				cmdMap.register("lockchat", lockChat);
				cmdMap.register("clearchat", clearChat);
				cmdMap.register("freeze", new CmdFreeze("freeze"));
				cmdMap.register("unfreeze", new CmdUnfreeze("unfreeze"));
				cmdMap.register("staffchat", new CmdStaffChat("staffchat"));
			}
			if (settingsConfig.getBoolean("scoreboard")) {
				HashMap<String, String> scorePrefixes = new HashMap<>();
				scorePrefixes.put("rank", getConfig().getString("scoreboard.rankPrefix"));
				scorePrefixes.put("online", getConfig().getString("scoreboard.onlinePrefix"));
				scorePrefixes.put("faction", getConfig().getString("scoreboard.factionPrefix"));
				scorePrefixes.put("money", getConfig().getString("scoreboard.moneyPrefix"));
				scorePrefixes.put("fly", getConfig().getString("scoreboard.flyPrefix"));
				scorePrefixes.put("combat", getConfig().getString("scoreboard.combatPrefix"));
				scorePrefixes.put("enderpearl", getConfig().getString("scoreboard.enderpearlPrefix"));
				scorePrefixes.put("koth", getConfig().getString("scoreboard.kothPrefix"));
				if (luckPermsHook) {
					new LuckPermsHook();
				}
				ScoreboardHandler scoreHandler = new ScoreboardHandler(
						getConfig().getString("scoreboard.scoreboardTitle"), scorePrefixes);
				Bukkit.getServer().getPluginManager().registerEvents(scoreHandler, this);
				if (essentialsHook) {
					Bukkit.getServer().getPluginManager().registerEvents(new EssentialsHookEvents(), this);
				}
				if (subsideKothHook) {
					Bukkit.getServer().getPluginManager().registerEvents(new KothHookEvents(), this);
				}
				if (permissionsExFound) {
					Bukkit.getServer().getPluginManager().registerEvents(new PermissionsExHookEvents(), this);
				}
				scoreboardEnabled = true;
			}
			if (settingsConfig.getBoolean("customMessages")) {
				ArrayList<String> joinMessages = new ArrayList<>();
				for (String s : getConfig().getStringList("misc.customJoinMessages")) {
					joinMessages.add(s);
				}
				CustomMessages customMessages = new CustomMessages(joinMessages,
						getConfig().getString("misc.customLeaveMessage"));
				Bukkit.getServer().getPluginManager().registerEvents(customMessages, this);
			}
			if (settingsConfig.getBoolean("territoryManagement")) {
				PlayerManagement playerManagement = new PlayerManagement(
						getConfig().getBoolean("playerManagement.logoutInOtherTerritory"),
						getConfig().getInt("playerManagement.secondsToRemoveHomes"));
				Bukkit.getServer().getPluginManager().registerEvents(playerManagement, this);
			}

			if (!getConfig().getBoolean("misc.waterBreaksRedstone")) {
				Bukkit.getServer().getPluginManager().registerEvents(new WaterInteractRedstone(), this);
			}

			if (settingsConfig.getBoolean("borderPatches")) {
				WorldborderPatches worldborderPatches = new WorldborderPatches(
						getConfig().getBoolean("misc.stackingOnWorldborder"));
				Bukkit.getServer().getPluginManager().registerEvents(worldborderPatches, this);
			}

			if (settingsConfig.getBoolean("webLimit")) {
				WebLimiter webLimit = new WebLimiter(getConfig().getInt("misc.webLimit"),
						getConfig().getString("misc.webLimitReachedMessage"));
				Bukkit.getServer().getPluginManager().registerEvents(webLimit, this);
			}
			if (getConfig().getBoolean("misc.silentTNT")) {
				if ((Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3])
						.contains("v1_8")) {
					new SilentTNT();
				}
			}
			if (getConfig().getBoolean("misc.useCrophopper")) {
				Bukkit.getServer().getPluginManager().registerEvents(new CropHopper(), this);
			}
			initSuccess = true;

		} catch (NumberFormatException ex1) {
			logConsoleError("Configuration File Import Number Error! - " + ex1.getMessage());
			Bukkit.getPluginManager().disablePlugin(this);
			return false;
		} catch (IllegalArgumentException ex2) {
			logConsoleError("Configuration File Import Enum Error! - " + ex2.getMessage());
			Bukkit.getPluginManager().disablePlugin(this);
			return false;
		} catch (NullPointerException ex3) {
			logConsoleError("Configuration File Access Error! - " + ex3.getMessage());
			Bukkit.getPluginManager().disablePlugin(this);
			return false;
		}
		Bukkit.getServer().getPluginManager().registerEvents(new EmptyBottleRemover(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new JellyLegs(), this);
		return initSuccess;
	}

	public boolean checkDatabaseConnection() {
		String password = getConfig().getString("database.password");
		String username = getConfig().getString("database.username");
		String database = getConfig().getString("database.name");
		String address = getConfig().getString("database.IP");
		String port = getConfig().getString("database.port");
		try {
			ds.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + database);
			ds.setUsername(username);
			ds.setPassword(password);
			Connection c = ds.getConnection();
			c.close();
			logConsoleMessage("Database connection established!");
			return true;
		} catch (Exception e) {
			logConsoleError("Database Credentials are invalid! Plugin will be disabled");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return false;
		}
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			econ = economyProvider.getProvider();
		}
		return (econ != null);
	}

	public static void logConsoleError(String message) {
		Bukkit.getServer().getConsoleSender().sendMessage(
				Utils.cc("&4-------------------------------------------------------------------------------------"));
		// Bukkit.getServer().getLogger().log(Level.SEVERE,
		// "§4----------------------------------------------------------------");
		Bukkit.getServer().getConsoleSender().sendMessage(Utils.cc("&4[FAIO ERROR] " + message));
		// Bukkit.getServer().getLogger().log(Level.SEVERE,
		// "§4----------------------------------------------------------------");
		Bukkit.getServer().getConsoleSender().sendMessage(
				Utils.cc("&4-------------------------------------------------------------------------------------"));
	}

	public static void logConsoleMessage(String message) {
		Bukkit.getServer().getLogger().log(Level.INFO,
				"-------------------------------------------------------------------------------------");
		Bukkit.getServer().getLogger().log(Level.INFO, "[FAIO] " + message);
		Bukkit.getServer().getLogger().log(Level.INFO,
				"-------------------------------------------------------------------------------------");
	}

	public void checkDependancies() {
		if (!setupEconomy()) {
			logConsoleError("Vault Dependancy cannot be found! Disabling plugin");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return;
		} else {
			logConsoleMessage("Hook: Vault - Successful");
		}
		if (Bukkit.getPluginManager().getPlugin("Factions") == null) {
			logConsoleError("Factions Dependancy cannot be found! Disabling plugin");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return;
		} else {
			logConsoleMessage("Hook: FactionsUUID - Successful");
		}
		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
			logConsoleError("ProtocolLib Dependancy cannot be found! Disabling plugin");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return;
		} else {
			logConsoleMessage("Hook: ProtocolLib - Successful");
		}
		if (Bukkit.getPluginManager().getPlugin("PermissionsEx") != null) {
			logConsoleMessage("Hook: PermissionsEx - Successful");
			permissionsExFound = true;
		}
		if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null && !permissionsExFound) {
			luckPermsHook = true;
			logConsoleMessage("Hook: LuckPerms - Successful");
		}
		if (Bukkit.getPluginManager().getPlugin("Essentials") == null) {
			logConsoleError("Essentials Dependancy cannot be found! Scoreboard will not update player money!");
		} else {
			logConsoleMessage("Hook: Essentials - Successful");
			essentialsPlugin = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			essentialsHook = true;
		}
		if (Bukkit.getPluginManager().getPlugin("KoTH") != null) {
			logConsoleMessage("Hook: Subside Koth - Successful");
			subsideKothHook = true;
		}
		if (Bukkit.getPluginManager().getPlugin("SilkSpawners") != null) {
			logConsoleMessage("Hook: SilkSpawners - Successful");
		}
	}

	public void checkCratesFile() {
		crates = new File(getDataFolder(), "crates.yml");
		if (!crates.exists()) {
			try {
				crates.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			crateConfig = YamlConfiguration.loadConfiguration(crates);
		} else {
			crateConfig = YamlConfiguration.loadConfiguration(crates);
		}
	}

	public static void saveCratesFile() {
		try {
			crateConfig.save(crates);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void importCrates() {
		if (!cratesImport) {
			return;
		}
		crateNames = new ArrayList<String>();
		if (crateConfig.getConfigurationSection("crates") == null) {
			CrateSQL.initCratesTbl(crateNames);
			return;
		}
		for (String key : crateConfig.getConfigurationSection("crates").getKeys(false)) {
			crateNames.add(key);
			CrateObject c = new CrateObject();
			c.setName(key);
			c.setEffect(Effect.valueOf(crateConfig.getString("crates." + key + ".particleEffect")));
			String[] split = crateConfig.getString("crates." + key + ".location").split(",");
			if (Bukkit.getWorld(split[3])
					.getBlockAt(new Location(Bukkit.getWorld(split[3]), Integer.parseInt(split[0]),
							Integer.parseInt(split[1]), Integer.parseInt(split[2])))
					.getType() != Material.ENDER_PORTAL_FRAME) {
				crateNames.remove(key);
				continue;
			}
			c.setCrateLoc(new Location(Bukkit.getWorld(split[3]), Integer.parseInt(split[0]),
					Integer.parseInt(split[1]), Integer.parseInt(split[2])));
			for (Entity e : c.getCrateLoc().getWorld().getNearbyEntities(c.getCrateLoc(), 1, 1, 1)) {
				if (e.getType() == EntityType.ARMOR_STAND) {
					ArmorStand a = (ArmorStand) e;
					if (ChatColor.stripColor(a.getCustomName()).contains(c.getName())) {
						c.setHologram(a);
						break;
					}
				}
			}
			if (c.getHologram() == null) {
				ArmorStand as = (ArmorStand) c.getCrateLoc().getWorld().spawnEntity(
						new Location(c.getCrateLoc().getWorld(), c.getCrateLoc().getBlockX() + 0.500,
								c.getCrateLoc().getBlockY() - 1, c.getCrateLoc().getBlockZ() + 0.500),
						EntityType.ARMOR_STAND);
				as.setGravity(false);
				as.setSmall(true);
				as.setCanPickupItems(false);
				as.setCustomName(
						"  " + Utils.cc(getConfig().getString("crates.hologramTitleFormat").replace("%name%", key)));
				as.setCustomNameVisible(true);
				as.setVisible(false);
				c.setHologram(as);
			}
			int taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

				@Override
				public void run() {
					c.getCrateLoc().getWorld().playEffect(c.getCrateLoc(), c.getEffect(), 10, 20);
				}
			}, 0, 10);
			c.setTaskID(taskID);
			Inventory inv = Bukkit.createInventory(null, 45, c.getName() + " Crate");
			for (int j = 0; j < 9; j++) {
				inv.setItem(j, new ItemStack(Material.STAINED_GLASS_PANE));
			}
			for (int j = 36; j < 45; j++) {
				inv.setItem(j, new ItemStack(Material.STAINED_GLASS_PANE));
			}
			int index = 9;
			for (String item : crateConfig.getConfigurationSection("crates." + key + ".contents").getKeys(false)) {
				ItemStack i = new ItemStack(Material.getMaterial(
						FAIOPlugin.getCratesConfig().getInt("crates." + key + ".contents." + item + ".itemID")));
				ItemMeta m = i.getItemMeta();
				ArrayList<String> iLore = new ArrayList<String>();
				iLore.add(ItemDataUtil.encodeString(FAIOPlugin.getCratesConfig()
						.getString("crates." + key + ".contents." + item + ".chanceToWinPercent") + "#"
						+ FAIOPlugin.getCratesConfig()
								.getString("crates." + key + ".contents." + item + ".commandToExecuteOnWin")));
				for (String s : FAIOPlugin.getCratesConfig()
						.getStringList("crates." + key + ".contents." + item + ".itemLore")) {
					iLore.add(Utils.cc(s));
				}
				m.setDisplayName(Utils.cc(
						FAIOPlugin.getCratesConfig().getString("crates." + key + ".contents." + item + ".itemName")));
				m.setLore(iLore);
				i.setItemMeta(m);
				inv.setItem(index, i);
				index++;
			}
			c.setI(inv);
			c.setDefaultContents(inv.getContents());
			Crates.getActiveCrates().add(c);
		}
		logConsoleMessage("Crates successfully imported!");
		CrateSQL.initCratesTbl(crateNames);
	}

	public static void initFtop(FTop ftop, boolean delay) {
		int delayTicks = 0;
		if (delay) {
			delayTicks = 1200;
		}
		ftopTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {

			@Override
			public void run() {
				logConsoleMessage("Syncing FTop Values, this may take a while!");
				ftop.calculateChunkWorth();
			}
		}, delayTicks, 30 * 20 * 60);
	}

	public static Essentials getEssentials() {
		return essentialsPlugin;
	}
}
