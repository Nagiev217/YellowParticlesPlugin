package com.server.towerdefense.visual;

import org.bukkit.Material;

public class TowerModelData {
    private final boolean enabled;
    private final Material itemMaterial;
    private final int customModelData;
    private final float scale;
    private final float rotationYaw;

    public TowerModelData(boolean enabled, Material itemMaterial, int customModelData, float scale, float rotationYaw) {
        this.enabled = enabled;
        this.itemMaterial = itemMaterial;
        this.customModelData = customModelData;
        this.scale = scale;
        this.rotationYaw = rotationYaw;
    }

    public boolean isEnabled() {
        return enabled;
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

    public float getRotationYaw() {
        return rotationYaw;
    }
}
