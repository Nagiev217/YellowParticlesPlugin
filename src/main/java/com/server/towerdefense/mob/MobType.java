package com.server.towerdefense.mob;

import org.bukkit.entity.EntityType;

public enum MobType {
    NORMAL("normal", EntityType.ZOMBIE),
    FAST("fast", EntityType.SPIDER),
    TANK("tank", EntityType.HUSK),
    BOSS("boss", EntityType.RAVAGER);

    private final String configKey;
    private final EntityType entityType;

    MobType(String configKey, EntityType entityType) {
        this.configKey = configKey;
        this.entityType = entityType;
    }

    public String getConfigKey() {
        return configKey;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
