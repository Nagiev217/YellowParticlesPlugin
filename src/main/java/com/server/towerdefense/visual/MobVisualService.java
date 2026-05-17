package com.server.towerdefense.visual;

import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.mob.MobType;
import com.server.towerdefense.mob.TDMob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;

public class MobVisualService {
    private final ConfigManager configManager;
    private final ModelDisplayManager displayManager;

    public MobVisualService(ConfigManager configManager, ModelDisplayManager displayManager) {
        this.configManager = configManager;
        this.displayManager = displayManager;
    }

    public MobModelData getModelData(MobType type) {
        String root = "models.mobs." + type.getConfigKey() + ".";
        boolean enabled = configManager.getConfig().getBoolean("models.enabled", false);
        EntityType baseEntity = parseEntity(configManager.getConfig().getString(root + "base-entity"), type.getEntityType());
        Material material = Material.matchMaterial(configManager.getConfig().getString(root + "item", "PAPER"));
        int customModelData = configManager.getConfig().getInt(root + "custom-model-data", 2000);
        float scale = (float) configManager.getConfig().getDouble(root + "scale", 1.0);
        double yOffset = configManager.getConfig().getDouble(root + "y-offset", 0.0);
        return new MobModelData(enabled, baseEntity, material == null ? Material.PAPER : material, customModelData, scale, yOffset);
    }

    public void applyVisual(TDMob mob) {
        LivingEntity entity = mob.getEntity();
        entity.setCustomNameVisible(true);
        MobModelData modelData = getModelData(mob.getType());
        if (!modelData.isEnabled()) {
            return;
        }
        entity.setInvisible(true);
        entity.setSilent(true);
        Location displayLocation = entity.getLocation().clone().add(0, modelData.getYOffset(), 0);
        ItemDisplay display = displayManager.spawnItemDisplay(displayLocation, modelData.getItemMaterial(), modelData.getCustomModelData(), modelData.getScale(), 0.0f);
        displayManager.tagMobDisplay(display, mob.getId());
        mob.setDisplayEntityId(display.getUniqueId());
    }

    public void updateMobVisual(TDMob mob) {
        if (mob.getDisplayEntityId() == null) {
            return;
        }
        Entity entity = mob.getEntity().getWorld().getEntity(mob.getDisplayEntityId());
        if (!(entity instanceof ItemDisplay display) || !display.isValid()) {
            return;
        }
        MobModelData modelData = getModelData(mob.getType());
        display.teleport(mob.getEntity().getLocation().clone().add(0, modelData.getYOffset(), 0));
    }

    public void removeMobVisual(TDMob mob) {
        if (mob.getDisplayEntityId() == null) {
            return;
        }
        Entity entity = mob.getEntity().getWorld().getEntity(mob.getDisplayEntityId());
        if (entity != null) {
            entity.remove();
        }
        mob.setDisplayEntityId(null);
    }

    private EntityType parseEntity(String raw, EntityType fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            EntityType entityType = EntityType.valueOf(raw.toUpperCase());
            return entityType.isAlive() ? entityType : fallback;
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }
}
