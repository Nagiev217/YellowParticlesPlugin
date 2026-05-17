package com.server.towerdefense.mob;

import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class TDMob {
    private final UUID id;
    private final LivingEntity entity;
    private final MobType type;
    private final double maxHp;
    private final double baseSpeed;
    private final int reward;
    private double hp;
    private int pathIndex = 1;
    private double routeProgress;
    private double slowMultiplier = 1.0;
    private long slowUntilTick;
    private boolean towerAttackAttempted;
    private UUID displayEntityId;

    public TDMob(UUID id, LivingEntity entity, MobType type, double maxHp, double baseSpeed, int reward) {
        this.id = id;
        this.entity = entity;
        this.type = type;
        this.maxHp = maxHp;
        this.baseSpeed = baseSpeed;
        this.reward = reward;
        this.hp = maxHp;
    }

    public UUID getId() {
        return id;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public MobType getType() {
        return type;
    }

    public double getMaxHp() {
        return maxHp;
    }

    public double getHp() {
        return hp;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public int getReward() {
        return reward;
    }

    public UUID getDisplayEntityId() {
        return displayEntityId;
    }

    public void setDisplayEntityId(UUID displayEntityId) {
        this.displayEntityId = displayEntityId;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public void setPathIndex(int pathIndex) {
        this.pathIndex = pathIndex;
    }

    public double getRouteProgress() {
        return routeProgress;
    }

    public void setRouteProgress(double routeProgress) {
        this.routeProgress = routeProgress;
    }

    public double getEffectiveSpeed(long currentTick) {
        if (slowUntilTick <= currentTick) {
            slowMultiplier = 1.0;
        }
        return baseSpeed * slowMultiplier;
    }

    public void applySlow(double slowPercent, int durationTicks, long currentTick) {
        double clamped = Math.max(0.0, Math.min(100.0, slowPercent));
        slowMultiplier = 1.0 - (clamped / 100.0);
        slowUntilTick = Math.max(slowUntilTick, currentTick + durationTicks);
    }

    public boolean damage(double amount) {
        hp = Math.max(0.0, hp - amount);
        entity.setHealth(Math.max(0.1, Math.min(entity.getMaxHealth(), hp)));
        entity.setCustomName(formatName());
        return hp <= 0.0;
    }

    public boolean hasAttemptedTowerAttack() {
        return towerAttackAttempted;
    }

    public void markTowerAttackAttempted() {
        towerAttackAttempted = true;
    }

    public String formatName() {
        return type.name() + " " + Math.ceil(hp) + "/" + Math.ceil(maxHp);
    }
}
