package com.server.towerdefense.tower;

import org.bukkit.Material;

public enum TowerType {
    ARCHER_TOWER("archer", Material.DISPENSER, Material.OAK_FENCE),
    CANNON_TOWER("cannon", Material.BLAST_FURNACE, Material.STONE_BRICK_WALL),
    ICE_TOWER("ice", Material.BLUE_STAINED_GLASS, Material.PACKED_ICE);

    private final String configKey;
    private final Material topMaterial;
    private final Material baseMaterial;

    TowerType(String configKey, Material topMaterial, Material baseMaterial) {
        this.configKey = configKey;
        this.topMaterial = topMaterial;
        this.baseMaterial = baseMaterial;
    }

    public String getConfigKey() {
        return configKey;
    }

    public Material getTopMaterial() {
        return topMaterial;
    }

    public Material getBaseMaterial() {
        return baseMaterial;
    }

    public static TowerType fromCommand(String raw) {
        return switch (raw.toLowerCase()) {
            case "archer" -> ARCHER_TOWER;
            case "cannon" -> CANNON_TOWER;
            case "ice" -> ICE_TOWER;
            default -> null;
        };
    }
}
