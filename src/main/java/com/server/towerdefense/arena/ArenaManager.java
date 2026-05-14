package com.server.towerdefense.arena;

import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.mob.MobManager;
import com.server.towerdefense.path.PathPoint;
import com.server.towerdefense.tower.TowerManager;
import com.server.towerdefense.wave.WaveManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArenaManager {
    private final ConfigManager configManager;
    private final Map<String, Arena> arenas = new HashMap<>();
    private TowerManager towerManager;
    private MobManager mobManager;
    private WaveManager waveManager;

    public ArenaManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void setRuntimeManagers(TowerManager towerManager, MobManager mobManager, WaveManager waveManager) {
        this.towerManager = towerManager;
        this.mobManager = mobManager;
        this.waveManager = waveManager;
    }

    public void loadArenas() {
        arenas.clear();
        ConfigurationSection section = configManager.getConfig().getConfigurationSection("arenas");
        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            String root = "arenas." + id + ".";
            World world = Bukkit.getWorld(configManager.getConfig().getString(root + "world", "world"));
            if (world == null) {
                Bukkit.getLogger().warning("[TowerDefense] World for arena '" + id + "' is not loaded.");
                continue;
            }

            Location spawn = parseLocation(world, configManager.getConfig().getString(root + "mob-spawn"));
            Location base = parseLocation(world, configManager.getConfig().getString(root + "base"));
            List<PathPoint> path = new ArrayList<>();
            for (String raw : configManager.getConfig().getStringList(root + "path")) {
                path.add(new PathPoint(parseLocation(world, raw)));
            }

            if (spawn != null && base != null && path.size() >= 2) {
                arenas.put(id.toLowerCase(), new Arena(id, world, spawn, base, path));
            }
        }
    }

    public Optional<Arena> getArena(String id) {
        return Optional.ofNullable(arenas.get(id.toLowerCase()));
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public boolean startArena(String id) {
        Optional<Arena> optionalArena = getArena(id);
        if (optionalArena.isEmpty()) {
            return false;
        }
        Arena arena = optionalArena.get();
        arena.setRunning(true);
        arena.setCurrentWave(0);
        return true;
    }

    public boolean stopArena(String id) {
        Optional<Arena> optionalArena = getArena(id);
        if (optionalArena.isEmpty()) {
            return false;
        }
        Arena arena = optionalArena.get();
        arena.setRunning(false);
        if (waveManager != null) {
            waveManager.stopWave(arena);
        }
        if (mobManager != null) {
            mobManager.removeAll(arena);
        }
        if (towerManager != null) {
            towerManager.removeAll(arena);
        }
        arena.setCurrentWave(0);
        return true;
    }

    public Arena findArenaByLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return arenas.values().stream()
                .filter(Arena::isRunning)
                .filter(arena -> arena.getWorld().equals(location.getWorld()))
                .findFirst()
                .orElse(null);
    }

    private Location parseLocation(World world, String raw) {
        if (raw == null) {
            return null;
        }
        String[] parts = raw.split(",");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new Location(world,
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim()),
                    Double.parseDouble(parts[2].trim()));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
