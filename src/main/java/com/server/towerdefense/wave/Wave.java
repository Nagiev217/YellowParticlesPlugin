package com.server.towerdefense.wave;

import java.util.List;

public class Wave {
    private final int number;
    private final List<WaveMobEntry> entries;
    private final int startDelayTicks;
    private boolean active;
    private int spawnedMobs;
    private int totalMobs;

    public Wave(int number, List<WaveMobEntry> entries, int startDelayTicks) {
        this.number = number;
        this.entries = entries;
        this.startDelayTicks = startDelayTicks;
        this.totalMobs = entries.stream().mapToInt(WaveMobEntry::getAmount).sum();
    }

    public int getNumber() {
        return number;
    }

    public List<WaveMobEntry> getEntries() {
        return entries;
    }

    public int getStartDelayTicks() {
        return startDelayTicks;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    public boolean isSpawningStarted() {
        return spawningStarted;
    }

    public void setSpawningStarted(boolean spawningStarted) {
        this.spawningStarted = spawningStarted;
    }

    public int getSpawnedMobs() {
        return spawnedMobs;
    }

    public void incrementSpawnedMobs() {
        spawnedMobs++;
    }

    public int getTotalMobs() {
        return totalMobs;
    }

    public boolean hasSpawnedAll() {
        return spawnedMobs >= totalMobs;
    }
}
