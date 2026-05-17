package com.server.towerdefense.tower;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.visual.TowerVisualService;
import com.server.towerdefense.visual.TowerVisualEntities;
import org.bukkit.Location;
import org.bukkit.Material;
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

    private final ConfigManager configManager;
    private final TowerUpgradeService upgradeService;
    private final TowerVisualService visualService;
    private final NamespacedKey towerItemKey;

    public TowerManager(JavaPlugin plugin, ConfigManager configManager, TowerUpgradeService upgradeService, TowerVisualService visualService) {
        this.configManager = configManager;
        this.upgradeService = upgradeService;
        this.visualService = visualService;
        this.towerItemKey = new NamespacedKey(plugin, "tower_type");
    }

    public ItemStack createTowerItem(TowerType type) {
        Material itemMaterial = type.getTopMaterial();
        Integer customModelData = null;
        if (configManager.getConfig().getBoolean("models.enabled", false)) {
            var modelData = visualService.getModelData(type, 1);
            itemMaterial = modelData.getItemMaterial();
            customModelData = modelData.getCustomModelData();
        }
        ItemStack item = new ItemStack(itemMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(TOWER_ITEM_NAME + ": " + type.name());
        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
        }
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
        double maxHealth = configManager.getConfig().getDouble(root + "max-health", 100.0);
        int cost = getTowerCost(type);

        Location towerLocation = baseLocation.getBlock().getLocation();
        UUID towerId = UUID.randomUUID();
        TowerVisualEntities visualEntities = visualService.buildTower(towerLocation, type, 1, towerId);
        Tower tower = new Tower(towerId, owner.getUniqueId(), towerLocation, type, maxHealth, cost, upgradeService.getData(type, 1), visualEntities.healthDisplay());
        tower.setDisplayEntityId(visualEntities.displayId());
        tower.setInteractionEntityId(visualEntities.interactionId());
        arena.getActiveTowers().add(tower);
        return tower;
    }

    public int getTowerCost(TowerType type) {
        return configManager.getConfig().getInt("towers." + type.getConfigKey() + ".cost", 0);
    }

    public Optional<Tower> findAt(Arena arena, Location location) {
        return arena.getActiveTowers().stream()
                .filter(tower -> isTowerBlock(tower, location))
                .findFirst();
    }

    public Optional<Tower> findById(Arena arena, UUID towerId) {
        return arena.getActiveTowers().stream()
                .filter(tower -> tower.getId().equals(towerId))
                .findFirst();
    }

    public Optional<Tower> findNearestInRange(Arena arena, Location location, double range) {
        double rangeSquared = range * range;
        return arena.getActiveTowers().stream()
                .filter(tower -> tower.getLocation().getWorld().equals(location.getWorld()))
                .filter(tower -> tower.getLocation().add(0.5, 0.5, 0.5).distanceSquared(location) <= rangeSquared)
                .min((first, second) -> Double.compare(
                        first.getLocation().distanceSquared(location),
                        second.getLocation().distanceSquared(location)));
    }

    public void removeTower(Arena arena, Tower tower) {
        arena.getActiveTowers().remove(tower);
        visualService.removeTower(tower);
    }

    public double repairTower(Tower tower) {
        double amount = configManager.getConfig().getDouble("towers.repair.amount", 25.0);
        return tower.repair(amount);
    }

    public Material getRepairMaterial() {
        String rawMaterial = configManager.getConfig().getString("towers.repair.material", "IRON_INGOT");
        Material material = Material.matchMaterial(rawMaterial);
        return material == null ? Material.IRON_INGOT : material;
    }

    public void removeAll(Arena arena) {
        List<Tower> copy = new ArrayList<>(arena.getActiveTowers());
        for (Tower tower : copy) {
            visualService.removeTower(tower);
        }
        arena.getActiveTowers().clear();
    }

    private boolean isTowerBlock(Tower tower, Location location) {
        Block clickedBlock = location.getBlock();
        Location towerLocation = tower.getLocation();
        return towerLocation.getBlock().equals(clickedBlock)
                || towerLocation.clone().add(0, 1, 0).getBlock().equals(clickedBlock);
    }

    public NamespacedKey getTowerItemKey() {
        return towerItemKey;
    }

}
