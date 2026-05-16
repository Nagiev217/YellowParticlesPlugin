package com.server.towerdefense;

import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.base.BaseManager;
import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.economy.EconomyManager;
import com.server.towerdefense.listener.BuildRestrictionListener;
import com.server.towerdefense.listener.MenuListener;
import com.server.towerdefense.listener.TowerBreakListener;
import com.server.towerdefense.listener.TowerInteractListener;
import com.server.towerdefense.listener.TowerPlaceListener;
import com.server.towerdefense.mob.MobManager;
import com.server.towerdefense.mob.MobMovementTask;
import com.server.towerdefense.path.PathManager;
import com.server.towerdefense.scoreboard.ScoreboardManager;
import com.server.towerdefense.tower.TowerAttackTask;
import com.server.towerdefense.tower.TowerManager;
import com.server.towerdefense.tower.TowerType;
import com.server.towerdefense.tower.TowerUpgradeService;
import com.server.towerdefense.ui.TowerMenu;
import com.server.towerdefense.visual.TowerVisualService;
import com.server.towerdefense.wave.WaveManager;
import org.bukkit.Bukkit;
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
    private EconomyManager economyManager;
    private BaseManager baseManager;
    private ScoreboardManager scoreboardManager;
    private TowerUpgradeService upgradeService;
    private TowerMenu towerMenu;
    private BukkitTask movementTask;
    private BukkitTask attackTask;
    private BukkitTask scoreboardTask;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();

        PathManager pathManager = new PathManager();
        arenaManager = new ArenaManager(configManager);
        economyManager = new EconomyManager(configManager);
        baseManager = new BaseManager(configManager);
        upgradeService = new TowerUpgradeService(configManager);
        towerManager = new TowerManager(this, configManager, upgradeService, new TowerVisualService());
        mobManager = new MobManager(this, configManager);
        scoreboardManager = new ScoreboardManager(economyManager, baseManager, mobManager);
        waveManager = new WaveManager(this, configManager, mobManager);
        arenaManager.setRuntimeManagers(towerManager, mobManager, waveManager);
        arenaManager.setMatchManagers(economyManager, baseManager, scoreboardManager);
        waveManager.setRuntimeManagers(arenaManager, scoreboardManager);
        towerMenu = new TowerMenu(upgradeService, economyManager);

        arenaManager.loadArenas();
        waveManager.loadWaves();

        movementTask = new MobMovementTask(arenaManager, mobManager, pathManager, towerManager, configManager, baseManager, scoreboardManager).runTaskTimer(this, 1L, 1L);
        attackTask = new TowerAttackTask(arenaManager, mobManager, economyManager, scoreboardManager).runTaskTimer(this, 1L, 1L);
        scoreboardTask = Bukkit.getScheduler().runTaskTimer(this, () -> arenaManager.getArenas().stream()
                .filter(arena -> arena.isRunning())
                .forEach(arena -> {
                    baseManager.updateBossBar(arena);
                    scoreboardManager.update(arena);
                }), 20L, 20L);

        getServer().getPluginManager().registerEvents(new BuildRestrictionListener(arenaManager, towerManager, configManager), this);
        getServer().getPluginManager().registerEvents(new TowerPlaceListener(arenaManager, towerManager, economyManager, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new TowerInteractListener(arenaManager, towerManager, towerMenu), this);
        getServer().getPluginManager().registerEvents(new TowerBreakListener(arenaManager, towerManager), this);
        getServer().getPluginManager().registerEvents(new MenuListener(arenaManager, towerManager, upgradeService, economyManager, scoreboardManager, towerMenu), this);
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
        if (scoreboardTask != null) {
            scoreboardTask.cancel();
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
            case "nextwave" -> handleNextWave(sender, args);
            case "tower" -> handleTower(sender, args);
            case "money" -> handleMoney(sender);
            case "give" -> handleGive(sender, args);
            case "base" -> handleBase(sender);
            case "towers" -> handleTowers(sender);
            case "mobs" -> handleMobs(sender);
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
            waveManager.loadWaves();
            if (!arena.isRunning()) {
                sender.sendMessage("Cannot start wave: arena '" + args[1] + "' is not running. Use /td start " + args[1] + " first.");
                return;
            }
            if (waveManager.getWave(waveNumber).isEmpty()) {
                sender.sendMessage("Cannot start wave: wave " + waveNumber + " is not loaded. Loaded waves: " + waveManager.getLoadedWaveNumbers());
                sender.sendMessage("Check plugins/TowerDefense/config.yml and make sure wave-list." + waveNumber + " exists.");
                return;
            }
            if (waveManager.startWave(arena, waveNumber)) {
                sender.sendMessage("Started wave " + waveNumber + " on arena " + args[1] + ".");
            } else {
                sender.sendMessage("Cannot start wave due to an internal state check. Loaded waves: " + waveManager.getLoadedWaveNumbers());
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

    private void handleNextWave(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /td nextwave <arena>");
            return;
        }
        arenaManager.getArena(args[1]).ifPresentOrElse(arena -> {
            if (waveManager.startNextWave(arena)) {
                sender.sendMessage("Started next wave.");
            } else {
                sender.sendMessage("No next wave available.");
            }
        }, () -> sender.sendMessage("Arena not found: " + args[1]));
    }

    private void handleMoney(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players have match money.");
            return;
        }
        var arena = arenaManager.findArenaByLocation(player.getLocation());
        if (arena == null) {
            player.sendMessage("You are not in a running arena world.");
            return;
        }
        player.sendMessage("Money: " + economyManager.getBalance(arena, player.getUniqueId()));
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 4 || !args[1].equalsIgnoreCase("money")) {
            sender.sendMessage("Usage: /td give money <player> <amount>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException exception) {
            sender.sendMessage("Amount must be a number.");
            return;
        }
        var arena = arenaManager.findArenaByLocation(target.getLocation());
        if (arena == null) {
            sender.sendMessage("Target is not in a running arena world.");
            return;
        }
        economyManager.addMoney(arena, target.getUniqueId(), amount);
        scoreboardManager.update(arena);
        sender.sendMessage("Added " + amount + " money to " + target.getName() + ".");
    }

    private void handleBase(CommandSender sender) {
        ArenaForSender result = findArenaForSender(sender);
        if (result.arena == null) {
            sender.sendMessage("No running arena found for you.");
            return;
        }
        var base = baseManager.getBase(result.arena);
        sender.sendMessage("Base HP: " + base.getCurrentHp() + "/" + base.getMaxHp());
    }

    private void handleTowers(CommandSender sender) {
        ArenaForSender result = findArenaForSender(sender);
        if (result.arena == null) {
            sender.sendMessage("No running arena found for you.");
            return;
        }
        sender.sendMessage("Towers: " + result.arena.getActiveTowers().size());
    }

    private void handleMobs(CommandSender sender) {
        ArenaForSender result = findArenaForSender(sender);
        if (result.arena == null) {
            sender.sendMessage("No running arena found for you.");
            return;
        }
        sender.sendMessage("Mobs: " + mobManager.getLivingMobs(result.arena).size());
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
        sender.sendMessage("/td nextwave test");
        sender.sendMessage("/td tower archer|cannon|ice");
        sender.sendMessage("/td money | /td base | /td towers | /td mobs");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("start", "stop", "wave", "nextwave", "tower", "money", "give", "base", "towers", "mobs", "reload"), args[0]);
        }
        if (args.length == 2 && List.of("start", "stop", "wave", "nextwave").contains(args[0].toLowerCase())) {
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

    private ArenaForSender findArenaForSender(CommandSender sender) {
        if (sender instanceof Player player) {
            return new ArenaForSender(arenaManager.findArenaByLocation(player.getLocation()));
        }
        return new ArenaForSender(arenaManager.getArenas().stream().filter(arena -> arena.isRunning()).findFirst().orElse(null));
    }

    private record ArenaForSender(com.server.towerdefense.arena.Arena arena) {
    }
}
