package com.server.towerdefense;

import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.listener.TowerPlaceListener;
import com.server.towerdefense.mob.MobManager;
import com.server.towerdefense.mob.MobMovementTask;
import com.server.towerdefense.path.PathManager;
import com.server.towerdefense.tower.TowerAttackTask;
import com.server.towerdefense.tower.TowerManager;
import com.server.towerdefense.tower.TowerType;
import com.server.towerdefense.wave.WaveManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TowerDefensePlugin extends JavaPlugin implements CommandExecutor, TabCompleter {
    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private TowerManager towerManager;
    private MobManager mobManager;
    private WaveManager waveManager;
    private BukkitTask movementTask;
    private BukkitTask attackTask;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();

        PathManager pathManager = new PathManager();
        arenaManager = new ArenaManager(configManager);
        towerManager = new TowerManager(this, configManager);
        mobManager = new MobManager(this, configManager);
        waveManager = new WaveManager(this, configManager, mobManager);
        arenaManager.setRuntimeManagers(towerManager, mobManager, waveManager);

        arenaManager.loadArenas();
        waveManager.loadWaves();

        movementTask = new MobMovementTask(arenaManager, mobManager, pathManager).runTaskTimer(this, 1L, 1L);
        attackTask = new TowerAttackTask(arenaManager, mobManager, configManager).runTaskTimer(this, 1L, 1L);

        getServer().getPluginManager().registerEvents(new TowerPlaceListener(arenaManager, towerManager), this);
        if (getCommand("td") != null) {
            getCommand("td").setExecutor(this);
            getCommand("td").setTabCompleter(this);
        }

        getLogger().info("TowerDefense enabled.");
    }

    @Override
    public void onDisable() {
        if (movementTask != null) {
            movementTask.cancel();
        }
        if (attackTask != null) {
            attackTask.cancel();
        }
        for (var arena : arenaManager.getArenas()) {
            arenaManager.stopArena(arena.getId());
        }
        getLogger().info("TowerDefense disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> handleStart(sender, args);
            case "stop" -> handleStop(sender, args);
            case "wave" -> handleWave(sender, args);
            case "tower" -> handleTower(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleStart(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /td start <arena>");
            return;
        }
        if (arenaManager.startArena(args[1])) {
            sender.sendMessage("Started arena " + args[1] + ".");
        } else {
            sender.sendMessage("Arena not found: " + args[1]);
        }
    }

    private void handleStop(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /td stop <arena>");
            return;
        }
        if (arenaManager.stopArena(args[1])) {
            sender.sendMessage("Stopped arena " + args[1] + ".");
        } else {
            sender.sendMessage("Arena not found: " + args[1]);
        }
    }

    private void handleWave(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /td wave <arena> <number>");
            return;
        }
        int waveNumber;
        try {
            waveNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage("Wave number must be a number.");
            return;
        }

        arenaManager.getArena(args[1]).ifPresentOrElse(arena -> {
            if (waveManager.startWave(arena, waveNumber)) {
                sender.sendMessage("Started wave " + waveNumber + " on arena " + args[1] + ".");
            } else {
                sender.sendMessage("Cannot start wave. Make sure arena is running and wave exists.");
            }
        }, () -> sender.sendMessage("Arena not found: " + args[1]));
    }

    private void handleTower(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive tower items.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("Usage: /td tower <archer|cannon|ice>");
            return;
        }
        TowerType type = TowerType.fromCommand(args[1]);
        if (type == null) {
            player.sendMessage("Unknown tower type. Use archer, cannon, or ice.");
            return;
        }
        player.getInventory().addItem(towerManager.createTowerItem(type));
        player.sendMessage("Given " + type.name() + " item.");
    }

    private void handleReload(CommandSender sender) {
        for (var arena : new ArrayList<>(arenaManager.getArenas())) {
            arenaManager.stopArena(arena.getId());
        }
        configManager.load();
        arenaManager.loadArenas();
        waveManager.loadWaves();
        sender.sendMessage("TowerDefense config reloaded.");
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("/td start test");
        sender.sendMessage("/td stop test");
        sender.sendMessage("/td wave test 1");
        sender.sendMessage("/td tower archer|cannon|ice");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("start", "stop", "wave", "tower", "reload"), args[0]);
        }
        if (args.length == 2 && List.of("start", "stop", "wave").contains(args[0].toLowerCase())) {
            return filter(arenaManager.getArenas().stream().map(arena -> arena.getId()).toList(), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("tower")) {
            return filter(List.of("archer", "cannon", "ice"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("wave")) {
            return filter(List.of("1", "2", "3", "5"), args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lowered = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lowered)) {
                result.add(option);
            }
        }
        return result;
    }
}
