package com.server.towerdefense.listener;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.tower.TowerManager;
import com.server.towerdefense.tower.TowerType;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class BuildRestrictionListener implements Listener {
    private final ArenaManager arenaManager;
    private final TowerManager towerManager;
    private final ConfigManager configManager;

    public BuildRestrictionListener(ArenaManager arenaManager, TowerManager towerManager, ConfigManager configManager) {
        this.arenaManager = arenaManager;
        this.towerManager = towerManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onTowerPlaceAttempt(PlayerInteractEvent event) {
        TowerType type = towerManager.readTowerItem(event.getItem());
        if (type == null || event.getClickedBlock() == null) {
            return;
        }
        Location placeLocation = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
        Arena arena = arenaManager.findArenaByLocation(placeLocation);
        if (arena == null || isInBuildZone(arena, placeLocation)) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage("You cannot build towers here.");
    }

    private boolean isInBuildZone(Arena arena, Location location) {
        String root = "arenas." + arena.getId() + ".build-zone.";
        if (!configManager.getConfig().contains(root + "min") || !configManager.getConfig().contains(root + "max")) {
            return true;
        }
        Location min = parse(arena, configManager.getConfig().getString(root + "min"));
        Location max = parse(arena, configManager.getConfig().getString(root + "max"));
        if (min == null || max == null) {
            return true;
        }
        double x1 = Math.min(min.getX(), max.getX());
        double y1 = Math.min(min.getY(), max.getY());
        double z1 = Math.min(min.getZ(), max.getZ());
        double x2 = Math.max(min.getX(), max.getX());
        double y2 = Math.max(min.getY(), max.getY());
        double z2 = Math.max(min.getZ(), max.getZ());
        return location.getX() >= x1 && location.getX() <= x2
                && location.getY() >= y1 && location.getY() <= y2
                && location.getZ() >= z1 && location.getZ() <= z2;
    }

    private Location parse(Arena arena, String raw) {
        if (raw == null) {
            return null;
        }
        String[] parts = raw.split(",");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new Location(arena.getWorld(), Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()), Double.parseDouble(parts[2].trim()));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
