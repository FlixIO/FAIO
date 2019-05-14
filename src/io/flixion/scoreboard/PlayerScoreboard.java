package io.flixion.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.massivecraft.factions.FPlayers;

import io.flixion.main.FAIOPlugin;
import io.flixion.main.Utils;
import me.lucko.luckperms.LuckPerms;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import subside.plugins.koth.KothPlugin;
import subside.plugins.koth.gamemodes.RunningKoth;

public class PlayerScoreboard {
	
	private Scoreboard playerScoreboard;
	private Objective playerScoreboardObjective;
	
	private Team topline;
	private Team bottomline;
	
	private Team playerFaction;
	private Team playerBalance;
	private Team onlinePlayers;
	private Team factionsFly;
	private Team combatTag;
	private Team enderpearlTimer;
	private Team playerRank;
	private Team kothHandler;
	
	@SuppressWarnings("deprecation")
	public PlayerScoreboard(Player p) {
		super();
		playerScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		playerScoreboardObjective = playerScoreboard.registerNewObjective("dummy", "dummy");
		playerScoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		playerScoreboardObjective.setDisplayName(Utils.cc(ScoreboardHandler.scoreboardTitle));
		
		playerFaction = playerScoreboard.registerNewTeam("faction");
		playerBalance = playerScoreboard.registerNewTeam("balance");
		factionsFly = playerScoreboard.registerNewTeam("factionsFly");
		combatTag = playerScoreboard.registerNewTeam("combatTag");
		enderpearlTimer = playerScoreboard.registerNewTeam("enderpearlTimer");
		playerRank = playerScoreboard.registerNewTeam("playerRank");
		onlinePlayers = playerScoreboard.registerNewTeam("onlinePlayers");
		topline = playerScoreboard.registerNewTeam("topline");
		bottomline = playerScoreboard.registerNewTeam("bottomline");
		kothHandler = playerScoreboard.registerNewTeam("kothHandler");
		
		topline.addEntry(Utils.cc("----------------"));
		bottomline.addEntry(Utils.cc("---------------"));
		topline.setPrefix(Utils.cc("&m------"));
		bottomline.setPrefix(Utils.cc("&m-------"));
		playerScoreboardObjective.getScore("----------------").setScore(15);
		playerScoreboardObjective.getScore("---------------").setScore(0);
		
		registerScoreTeam(playerFaction, "faction", 12);
		registerScoreTeam(playerBalance, "money", 11);
		registerScoreTeam(factionsFly, "fly", 10);
		registerScoreTeam(onlinePlayers, "online", 13);
		if (Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).length() > 16) {
			combatTag.setPrefix(Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).substring(0, 16));
			combatTag.addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).length()));
		} else {
			combatTag.addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")));
		}
		if (Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).length() > 16) {
			enderpearlTimer.setPrefix(Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).substring(0, 16));
			enderpearlTimer.addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).length()));
		} else {
			enderpearlTimer.addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")));
		}
		if (FAIOPlugin.permissionsExFound) {
			String rankPrefix = ChatColor.stripColor(Utils.cc(PermissionsEx.getPermissionManager().getUser(p).getPrefix()));
			playerRank.setSuffix(Utils.cc(" &f") + rankPrefix);
			registerScoreTeam(playerRank, "rank", 14);
		}
		else if (FAIOPlugin.luckPermsHook) {
			playerRank.setSuffix(Utils.cc(" &f") + LuckPerms.getApi().getUser(p.getUniqueId()).getPrimaryGroup());
			registerScoreTeam(playerRank, "rank", 14);
		}
		
		playerFaction.setSuffix(Utils.cc(" &f") + FPlayers.getInstance().getByPlayer(p).getFaction().getTag());
		playerBalance.setSuffix(Utils.cc(" &f$") + (long) FAIOPlugin.getEco().getBalance(p.getName()) + "");
		onlinePlayers.setSuffix(Utils.cc(" &f") + Bukkit.getServer().getOnlinePlayers().size() + "");
		factionsFly.setSuffix(Utils.cc(" &fDisabled"));
		if (FAIOPlugin.subsideKothHook) {
			KothPlugin kothP = (KothPlugin) Bukkit.getPluginManager().getPlugin("KoTH");
			RunningKoth koth = null;
			if (kothP.getKothHandler().getRunningKoths().size() > 1) {
				koth = kothP.getKothHandler().getRunningKoths().get(0);
			}
			else {
				koth = kothP.getKothHandler().getRunningKoth();
			}
			if (koth != null) {
				if (Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", koth.getKoth().getName())).length() > 16) {
					kothHandler.setPrefix(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", koth.getKoth().getName())).substring(0, 16));
					kothHandler.addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", koth.getKoth().getName())).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", koth.getKoth().getName())).length()));
					playerScoreboardObjective.getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", koth.getKoth().getName())).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", koth.getKoth().getName())).length())).setScore(6);
				} else {
					kothHandler.addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", koth.getKoth().getName())));
					playerScoreboardObjective.getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get("koth").replaceAll("%kothName%", koth.getKoth().getName()))).setScore(6);
				}
				BukkitTask task = Bukkit.getScheduler().runTaskTimer(FAIOPlugin.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						if (p.isOnline()) {
							kothHandler.setSuffix(Utils.cc(" &f" + kothP.getKothHandler().getRunningKoth().getKoth().getKothHandler().getRunningKoth().getTimeObject().getTimeLeftFormatted()));
						}
					}
				},0 ,20);
				KothHookEvents.kothRunnables.put(p.getUniqueId(), task);
			}
		}
		
		p.setScoreboard(playerScoreboard);
	}
	
	public void initCombatTimerScoreboard () {
		if (Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).length() > 16) {
			playerScoreboardObjective.getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).length())).setScore(8);
		} else {
			playerScoreboardObjective.getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get("combat"))).setScore(8);
		}
	}
	
	public void setCombatTimerScoreboard (int time) {
		combatTag.setSuffix(Utils.cc(" &f" + time + ".0s"));
	}
	
	public void removeCombatTimerScoreboard() {
		if (Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).length() > 16) {
			playerScoreboard.resetScores(Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")).length()));
		} else {
			playerScoreboard.resetScores(Utils.cc(ScoreboardHandler.scorePrefixes.get("combat")));
		}
	}
	
	public void initEnderpearlTimerScoreboard () {
		if (Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).length() > 16) {
			playerScoreboardObjective.getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).length())).setScore(7);
		} else {
			playerScoreboardObjective.getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl"))).setScore(7);
		}
	}
	
	public void setEnderpearlTimerScoreboard (int time) {
		enderpearlTimer.setSuffix(Utils.cc(" &f" + time + ".0s"));
	}
	
	public void removeEnderpearlTimerScoreboard() {
		if (Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).length() > 16) {
			playerScoreboard.resetScores(Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")).length()));
		} else {
			playerScoreboard.resetScores(Utils.cc(ScoreboardHandler.scorePrefixes.get("enderpearl")));
		}
	}

	public Scoreboard getPlayerScoreboard() {
		return playerScoreboard;
	}

	public void setPlayerScoreboard(Scoreboard playerScoreboard) {
		this.playerScoreboard = playerScoreboard;
	}

	public Objective getPlayerScoreboardObjective() {
		return playerScoreboardObjective;
	}

	public Team getPlayerFaction() {
		return playerFaction;
	}

	public void setPlayerFaction(Team playerFaction) {
		this.playerFaction = playerFaction;
	}

	public Team getPlayerBalance() {
		return playerBalance;
	}

	public void setPlayerBalance(Team playerBalance) {
		this.playerBalance = playerBalance;
	}

	public Team getOnlinePlayers() {
		return onlinePlayers;
	}

	public void setOnlinePlayers(Team onlinePlayers) {
		this.onlinePlayers = onlinePlayers;
	}

	public Team getPlayerRank() {
		return playerRank;
	}

	public void setPlayerRank(Team playerRank) {
		this.playerRank = playerRank;
	}

	public Team getCombatTag() {
		return combatTag;
	}

	public void setCombatTag(Team combatTag) {
		this.combatTag = combatTag;
	}

	public Team getFactionsFly() {
		return factionsFly;
	}

	public void setFactionsFly(Team factionsFly) {
		this.factionsFly = factionsFly;
	}

	public Team getKothHandler() {
		return kothHandler;
	}

	public void setKothHandler(Team kothHandler) {
		this.kothHandler = kothHandler;
	}
	
	public void registerScoreTeam (Team team, String type, int score) {
		if (Utils.cc(ScoreboardHandler.scorePrefixes.get("rank")).length() > 16) {
			team.setPrefix(Utils.cc(ScoreboardHandler.scorePrefixes.get(type)).substring(0, 16));
			team.addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get(type)).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get(type)).length()));
			playerScoreboardObjective.getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get(type)).substring(16, Utils.cc(ScoreboardHandler.scorePrefixes.get(type)).length())).setScore(score);
		} else {
			team.addEntry(Utils.cc(ScoreboardHandler.scorePrefixes.get(type)));
			playerScoreboardObjective.getScore(Utils.cc(ScoreboardHandler.scorePrefixes.get(type))).setScore(score);
		}
	}
}
