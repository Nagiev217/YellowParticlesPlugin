package com.server.towerdefense.scoreboard;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.base.Base;
import com.server.towerdefense.base.BaseManager;
import com.server.towerdefense.economy.EconomyManager;
import com.server.towerdefense.mob.MobManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardManager {
    private final EconomyManager economyManager;
    private final BaseManager baseManager;
    private final MobManager mobManager;
    private final Map<String, GameScoreboard> scoreboards = new HashMap<>();

    public ScoreboardManager(EconomyManager economyManager, BaseManager baseManager, MobManager mobManager) {
        this.economyManager = economyManager;
        this.baseManager = baseManager;
        this.mobManager = mobManager;
    }

    public void update(Arena arena) {
        GameScoreboard gameScoreboard = scoreboards.computeIfAbsent(arena.getId().toLowerCase(), ignored -> createScoreboard());
        Objective objective = gameScoreboard.objective();
        Scoreboard scoreboard = gameScoreboard.scoreboard();
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        Base base = baseManager.getBase(arena);
        objective.getScore(ChatColor.YELLOW + "Wave: " + arena.getCurrentWave()).setScore(5);
        objective.getScore(ChatColor.RED + "Base HP: " + base.getCurrentHp() + "/" + base.getMaxHp()).setScore(4);
        objective.getScore(ChatColor.GREEN + "Mobs: " + mobManager.getLivingMobs(arena).size()).setScore(2);
        objective.getScore(ChatColor.AQUA + "Towers: " + arena.getActiveTowers().size()).setScore(1);

        for (Player player : arena.getWorld().getPlayers()) {
            objective.getScore(ChatColor.GOLD + "Money: " + economyManager.getBalance(arena, player.getUniqueId())).setScore(3);
            player.setScoreboard(scoreboard);
        }
    }

    public void clear(Arena arena) {
        scoreboards.remove(arena.getId().toLowerCase());
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player player : arena.getWorld().getPlayers()) {
            player.setScoreboard(main);
        }
    }

    private GameScoreboard createScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("td", "dummy", ChatColor.GOLD + "Tower Defence");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        return new GameScoreboard(scoreboard, objective);
    }
}
