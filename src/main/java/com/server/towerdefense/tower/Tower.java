package com.server.towerdefense.tower;

import org.bukkit.Location;

import java.util.UUID;

public class Tower {
    private final UUID id;
    private final UUID owner;
    private final Location location;
    private final TowerType type;
    private final double damage;
    private final double range;
    private final int attackSpeedTicks;
    private int level = 1;
    private TargetMode targetMode = TargetMode.FIRST;
    private long lastAttackTick;

    public Tower(UUID id, UUID owner, Location location, TowerType type, double damage, double range, int attackSpeedTicks) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.type = type;
        this.damage = damage;
        this.range = range;
        this.attackSpeedTicks = attackSpeedTicks;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location.clone();
    }

    public TowerType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public double getDamage() {
        return damage;
    }

    public double getRange() {
        return range;
    }

    public int getAttackSpeedTicks() {
        return attackSpeedTicks;
    }

    public TargetMode getTargetMode() {
        return targetMode;
    }

    public void setTargetMode(TargetMode targetMode) {
        this.targetMode = targetMode;
    }

    public boolean canAttack(long currentTick) {
        return currentTick - lastAttackTick >= attackSpeedTicks;
    }

    public void markAttack(long currentTick) {
        lastAttackTick = currentTick;
    }
}
