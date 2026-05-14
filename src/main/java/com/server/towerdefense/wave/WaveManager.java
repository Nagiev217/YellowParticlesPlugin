package com.server.towerdefense.wave;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.mob.MobManager;
import com.server.towerdefense.mob.MobType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WaveManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final MobManager mobManager;
    private final Map<Integer, Wave> waves = new HashMap<>();
    private final Map<String, List<BukkitTask>> activeTasks = new HashMap<>();

    public WaveManager(JavaPlugin plugin, ConfigManager configManager, MobManager mobManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.mobManager = mobManager;
    }

    public void loadWaves() {
        waves.clear();
        ConfigurationSection section = configManager.getConfig().getConfigurationSection("waves");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            int number;
            try {
                number = Integer.parseInt(key);
            } catch (NumberFormatException exception) {
                continue;
            }

            String root = "waves." + key + ".";
            int startDelayTicks = configManager.getConfig().getInt(root + "start-delay-ticks", 100);
            List<WaveMobEntry> entries = new ArrayList<>();
            for (Map<?, ?> rawEntry : configManager.getConfig().getMapList(root + "entries")) {
                try {
                    MobType type = MobType.valueOf(String.valueOf(rawEntry.get("type")).toUpperCase());
                    int amount = Integer.parseInt(String.valueOf(rawEntry.get("amount")));
                    int delay = Integer.parseInt(String.valueOf(rawEntry.get("spawn-delay-ticks")));
                    entries.add(new WaveMobEntry(type, amount, delay));
                } catch (IllegalArgumentException exception) {
                    Bukkit.getLogger().warning("[TowerDefense] Invalid wave entry in wave " + number + ".");
                }
            }
            waves.put(number, new Wave(number, entries, startDelayTicks));
        }
    }

    public boolean startWave(Arena arena, int number) {
        Wave template = waves.get(number);
        if (template == null || !arena.isRunning()) {
            return false;
        }

        stopWave(arena);
        Wave wave = copyWave(template);
        wave.setActive(true);
        arena.setCurrentWave(number);

        BukkitTask spawnTask = Bukkit.getScheduler().runTaskLater(plugin, () -> spawnEntries(arena, wave), wave.getStartDelayTicks());
        trackTask(arena, spawnTask);

        BukkitTask finishTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!arena.isRunning()) {
                stopWave(arena);
                return;
            }
            if (wave.hasSpawnedAll() && mobManager.getLivingMobs(arena).isEmpty()) {
                wave.setActive(false);
                cancelTasks(arena);
                startNextWaveIfPresent(arena, number);
            }
        }, 20L, 20L);
        trackTask(arena, finishTask);
        return true;
    }

    public void stopWave(Arena arena) {
        cancelTasks(arena);
    }

    public Optional<Wave> getWave(int number) {
        return Optional.ofNullable(waves.get(number));
    }

    private void spawnEntries(Arena arena, Wave wave) {
        long delay = 0L;
        for (WaveMobEntry entry : wave.getEntries()) {
            for (int i = 0; i < entry.getAmount(); i++) {
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (arena.isRunning()) {
                        mobManager.spawnMob(arena, entry.getType());
                        wave.incrementSpawnedMobs();
                    }
                }, delay);
                trackTask(arena, task);
                delay += Math.max(1, entry.getSpawnDelayTicks());
            }
        }
    }

    private void startNextWaveIfPresent(Arena arena, int currentNumber) {
        waves.keySet().stream()
                .filter(number -> number > currentNumber)
                .min(Comparator.naturalOrder())
                .ifPresent(next -> startWave(arena, next));
    }

    private Wave copyWave(Wave wave) {
        return new Wave(wave.getNumber(), wave.getEntries(), wave.getStartDelayTicks());
    }

    private void trackTask(Arena arena, BukkitTask task) {
        activeTasks.computeIfAbsent(arena.getId().toLowerCase(), ignored -> new ArrayList<>()).add(task);
    }

    private void cancelTasks(Arena arena) {
        List<BukkitTask> tasks = activeTasks.remove(arena.getId().toLowerCase());
        if (tasks == null) {
            return;
        }
        for (BukkitTask task : tasks) {
            task.cancel();
        }
    }
}
