package com.server.towerdefense.arena;

import com.server.towerdefense.mob.TDMob;
import com.server.towerdefense.path.PathPoint;
import com.server.towerdefense.tower.Tower;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private final String id;
    private final World world;
    private final Location mobSpawn;
    private final Location baseLocation;
    private final List<PathPoint> path;
    private final List<Tower> activeTowers = new ArrayList<>();
    private final List<TDMob> activeMobs = new ArrayList<>();
    private int currentWave;
    private boolean running;

    public Arena(String id, World world, Location mobSpawn, Location baseLocation, List<PathPoint> path) {
        this.id = id;
        this.world = world;
        this.mobSpawn = mobSpawn;
        this.baseLocation = baseLocation;
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public World getWorld() {
        return world;
    }

    public Location getMobSpawn() {
        return mobSpawn.clone();
    }

    public Location getBaseLocation() {
        return baseLocation.clone();
    }

    public List<PathPoint> getPath() {
        return path;
    }

    public List<Tower> getActiveTowers() {
        return activeTowers;
    }

    public List<TDMob> getActiveMobs() {
        return activeMobs;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = currentWave;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
