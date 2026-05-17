package com.server.towerdefense.visual;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class MobModelData {
    private final boolean enabled;
    private final EntityType baseEntity;
    private final Material itemMaterial;
    private final int customModelData;
    private final float scale;
    private final double yOffset;

    public MobModelData(boolean enabled, EntityType baseEntity, Material itemMaterial, int customModelData, float scale, double yOffset) {
        this.enabled = enabled;
        this.baseEntity = baseEntity;
        this.itemMaterial = itemMaterial;
        this.customModelData = customModelData;
        this.scale = scale;
        this.yOffset = yOffset;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public EntityType getBaseEntity() {
        return baseEntity;
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public float getScale() {
        return scale;
    }

    public double getYOffset() {
        return yOffset;
    }
}
