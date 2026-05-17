package com.server.towerdefense.visual;

import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.tower.Tower;
import com.server.towerdefense.tower.TowerType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;

import java.util.UUID;

public class TowerVisualService {
    private final ConfigManager configManager;
    private final ModelDisplayManager displayManager;

    public TowerVisualService(ConfigManager configManager, ModelDisplayManager displayManager) {
        this.configManager = configManager;
        this.displayManager = displayManager;
    }

    public TowerVisualEntities buildTower(Location location, TowerType type, int level, UUID towerId) {
        ArmorStand healthDisplay = createHealthDisplay(location);
        if (!configManager.getConfig().getBoolean("models.enabled", false)) {
            buildFallbackBlocks(location, type);
            return new TowerVisualEntities(healthDisplay, null, null);
        }

        TowerModelData modelData = getModelData(type, level);
        Location displayLocation = location.clone().add(0.5, 0.05, 0.5);
        ItemDisplay display = displayManager.spawnItemDisplay(displayLocation, modelData.getItemMaterial(), modelData.getCustomModelData(), modelData.getScale(), modelData.getRotationYaw());
        displayManager.tagTowerDisplay(display, towerId);

        Entity interaction = displayManager.spawnInteraction(location.clone().add(0.5, 0.9, 0.5), 1.4f * modelData.getScale(), 2.0f * modelData.getScale(), towerId);
        return new TowerVisualEntities(healthDisplay, display.getUniqueId(), interaction.getUniqueId());
    }

    public void updateTowerModel(Tower tower) {
        if (!configManager.getConfig().getBoolean("models.enabled", false) || tower.getDisplayEntityId() == null) {
            return;
        }
        Entity entity = tower.getLocation().getWorld().getEntity(tower.getDisplayEntityId());
        if (!(entity instanceof ItemDisplay display) || !display.isValid()) {
            return;
        }
        TowerModelData modelData = getModelData(tower.getType(), tower.getLevel());
        displayManager.updateItemDisplay(display, modelData.getItemMaterial(), modelData.getCustomModelData(), modelData.getScale(), modelData.getRotationYaw());
    }

    public void removeTower(Tower tower) {
        if (tower.getDisplayEntityId() != null) {
            Entity display = tower.getLocation().getWorld().getEntity(tower.getDisplayEntityId());
            if (display != null) {
                display.remove();
            }
        }
        if (tower.getInteractionEntityId() != null) {
            Entity interaction = tower.getLocation().getWorld().getEntity(tower.getInteractionEntityId());
            if (interaction != null) {
                interaction.remove();
            }
        }
        if (!configManager.getConfig().getBoolean("models.enabled", false)) {
            Location location = tower.getLocation();
            location.getBlock().setType(Material.AIR);
            location.clone().add(0, 1, 0).getBlock().setType(Material.AIR);
        }
        tower.removeHealthDisplay();
    }

    public TowerModelData getModelData(TowerType type, int level) {
        String root = "models.towers." + type.getConfigKey() + ".";
        Material material = Material.matchMaterial(configManager.getConfig().getString(root + "item", "PAPER"));
        int customModelData = configManager.getConfig().getInt(root + "levels." + level + ".custom-model-data", 1000);
        float scale = (float) configManager.getConfig().getDouble(root + "scale", 1.0);
        float yaw = (float) configManager.getConfig().getDouble(root + "rotation-yaw", 0.0);
        return new TowerModelData(configManager.getConfig().getBoolean("models.enabled", false), material == null ? Material.PAPER : material, customModelData, scale, yaw);
    }

    private ArmorStand createHealthDisplay(Location location) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0.5, 2.25, 0.5), EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setMarker(true);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setCustomNameVisible(true);
        return armorStand;
    }

    private void buildFallbackBlocks(Location location, TowerType type) {
        Block base = location.getBlock();
        Block top = location.clone().add(0, 1, 0).getBlock();
        base.setType(type.getBaseMaterial());
        top.setType(type.getTopMaterial());
    }
}
