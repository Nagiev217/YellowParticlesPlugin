package com.server.towerdefense.tower;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TowerManager {
    public static final String TOWER_ITEM_NAME = "Tower Defense Tower";

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final NamespacedKey towerItemKey;

    public TowerManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.towerItemKey = new NamespacedKey(plugin, "tower_type");
    }

    public ItemStack createTowerItem(TowerType type) {
        ItemStack item = new ItemStack(type.getTopMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(TOWER_ITEM_NAME + ": " + type.name());
        meta.getPersistentDataContainer().set(towerItemKey, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }

    public TowerType readTowerItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        String value = item.getItemMeta().getPersistentDataContainer().get(towerItemKey, PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        try {
            return TowerType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public Tower placeTower(Arena arena, Player owner, Location baseLocation, TowerType type) {
        String root = "towers." + type.getConfigKey() + ".";
        double damage = configManager.getConfig().getDouble(root + "damage");
        double range = configManager.getConfig().getDouble(root + "range");
        int attackSpeedTicks = configManager.getConfig().getInt(root + "attack-speed-ticks");

        Location towerLocation = baseLocation.getBlock().getLocation();
        buildVisual(towerLocation, type);

        Tower tower = new Tower(UUID.randomUUID(), owner.getUniqueId(), towerLocation, type, damage, range, attackSpeedTicks);
        arena.getActiveTowers().add(tower);
        return tower;
    }

    public Optional<Tower> findAt(Arena arena, Location location) {
        return arena.getActiveTowers().stream()
                .filter(tower -> tower.getLocation().getBlock().equals(location.getBlock()))
                .findFirst();
    }

    public void removeAll(Arena arena) {
        List<Tower> copy = new ArrayList<>(arena.getActiveTowers());
        for (Tower tower : copy) {
            removeVisual(tower);
        }
        arena.getActiveTowers().clear();
    }

    private void buildVisual(Location location, TowerType type) {
        Block base = location.getBlock();
        Block top = location.clone().add(0, 1, 0).getBlock();
        base.setType(type.getBaseMaterial());
        top.setType(type.getTopMaterial());
    }

    private void removeVisual(Tower tower) {
        Location location = tower.getLocation();
        location.getBlock().setType(org.bukkit.Material.AIR);
        location.clone().add(0, 1, 0).getBlock().setType(org.bukkit.Material.AIR);
    }

    public NamespacedKey getTowerItemKey() {
        return towerItemKey;
    }
}
