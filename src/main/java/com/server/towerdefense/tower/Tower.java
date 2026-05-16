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
    private final ArmorStand healthDisplay;
    private double damage;
    private double range;
    private int attackSpeedTicks;
    private double splashRadius;
    private double slowPercent;
    private int slowDurationTicks;
    private double health;
    private int level = 1;
    private int totalSpent;
    private TargetMode targetMode = TargetMode.FIRST;
    private long lastAttackTick;

    public Tower(UUID id, UUID owner, Location location, TowerType type, double maxHealth, int totalSpent, TowerUpgradeData upgradeData, ArmorStand healthDisplay) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.type = type;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.totalSpent = totalSpent;
        this.healthDisplay = healthDisplay;
        applyUpgradeData(upgradeData);
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

    public double getSplashRadius() {
        return splashRadius;
    }

    public double getSlowPercent() {
        return slowPercent;
    }

    public int getSlowDurationTicks() {
        return slowDurationTicks;
    }

    public int getTotalSpent() {
        return totalSpent;
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

    public double repair(double amount) {
        double before = health;
        health = Math.min(maxHealth, health + Math.max(0.0, amount));
        updateHealthDisplay();
        return health - before;
    }

    public boolean isFullHealth() {
        return health >= maxHealth;
    }

    public void addSpent(int amount) {
        totalSpent += Math.max(0, amount);
    }

    public void applyUpgradeData(TowerUpgradeData data) {
        this.level = data.getLevel();
        this.damage = data.getDamage();
        this.range = data.getRange();
        this.attackSpeedTicks = data.getAttackSpeedTicks();
        this.splashRadius = data.getSplashRadius();
        this.slowPercent = data.getSlowPercent();
        this.slowDurationTicks = data.getSlowDurationTicks();
        updateHealthDisplay();
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
