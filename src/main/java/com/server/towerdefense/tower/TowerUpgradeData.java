package com.server.towerdefense.tower;

public class TowerUpgradeData {
    private final int level;
    private final double damage;
    private final double range;
    private final int attackSpeedTicks;
    private final int upgradeCost;
    private final double splashRadius;
    private final double slowPercent;
    private final int slowDurationTicks;

    public TowerUpgradeData(int level, double damage, double range, int attackSpeedTicks, int upgradeCost,
                            double splashRadius, double slowPercent, int slowDurationTicks) {
        this.level = level;
        this.damage = damage;
        this.range = range;
        this.attackSpeedTicks = attackSpeedTicks;
        this.upgradeCost = upgradeCost;
        this.splashRadius = splashRadius;
        this.slowPercent = slowPercent;
        this.slowDurationTicks = slowDurationTicks;
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

    public int getUpgradeCost() {
        return upgradeCost;
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
}
