package com.server.towerdefense.tower;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public class Tower {
    private final UUID id;
    private final UUID owner;
    private final Location location;
    private final TowerType type;
    private final double maxHealth;
    private final double damage;
    private final double range;
    private final int attackSpeedTicks;
    private final ArmorStand healthDisplay;

    private double health;
    private int level = 1;
    private TargetMode targetMode = TargetMode.FIRST;
    private long lastAttackTick;

    public Tower(UUID id, UUID owner, Location location, TowerType type, double maxHealth,
                 double damage, double range, int attackSpeedTicks, ArmorStand healthDisplay) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.type = type;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.damage = damage;
        this.range = range;
        this.attackSpeedTicks = attackSpeedTicks;
        this.healthDisplay = healthDisplay;
        updateHealthDisplay();
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

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getHealth() {
        return health;
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

    public boolean damageTower(double amount) {
        health = Math.max(0.0, health - amount);
        updateHealthDisplay();
        return health <= 0.0;
    }

    public void removeHealthDisplay() {
        if (healthDisplay != null && healthDisplay.isValid()) {
            healthDisplay.remove();
        }
    }

    private void updateHealthDisplay() {
        if (healthDisplay != null && healthDisplay.isValid()) {
            healthDisplay.setCustomName(type.name() + " HP: " + Math.ceil(health) + "/" + Math.ceil(maxHealth));
        }
    }
}
