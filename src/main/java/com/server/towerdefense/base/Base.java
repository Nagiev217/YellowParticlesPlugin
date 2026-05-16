package com.server.towerdefense.base;

import org.bukkit.Location;

public class Base {
    private final Location location;
    private final int maxHp;
    private int currentHp;

    public Base(Location location, int maxHp) {
        this.location = location;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
    }

    public Location getLocation() {
        return location.clone();
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void damage(int amount) {
        currentHp = Math.max(0, currentHp - Math.max(0, amount));
    }

    public boolean isDestroyed() {
        return currentHp <= 0;
    }
}
