package com.server.towerdefense.wave;

import com.server.towerdefense.mob.MobType;

public class WaveMobEntry {
    private final MobType type;
    private final int amount;
    private final int spawnDelayTicks;

    public WaveMobEntry(MobType type, int amount, int spawnDelayTicks) {
        this.type = type;
        this.amount = amount;
        this.spawnDelayTicks = spawnDelayTicks;
    }

    public MobType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getSpawnDelayTicks() {
        return spawnDelayTicks;
    }
}
