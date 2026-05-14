package com.server.towerdefense.mob;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.path.PathManager;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class MobMovementTask extends BukkitRunnable {
    private final ArenaManager arenaManager;
    private final MobManager mobManager;
    private final PathManager pathManager;
    private long currentTick;

    public MobMovementTask(ArenaManager arenaManager, MobManager mobManager, PathManager pathManager) {
        this.arenaManager = arenaManager;
        this.mobManager = mobManager;
        this.pathManager = pathManager;
    }

    @Override
    public void run() {
        currentTick++;
        for (Arena arena : arenaManager.getArenas()) {
            if (!arena.isRunning()) {
                continue;
            }
            for (TDMob mob : mobManager.getLivingMobs(arena).toArray(new TDMob[0])) {
                moveMob(arena, mob);
            }
        }
    }

    private void moveMob(Arena arena, TDMob mob) {
        if (pathManager.hasReachedEnd(arena, mob.getPathIndex())) {
            mobManager.leakMob(arena, mob);
            return;
        }

        Location entityLocation = mob.getEntity().getLocation();
        Location target = pathManager.getPoint(arena, mob.getPathIndex());
        if (target == null) {
            mobManager.leakMob(arena, mob);
            return;
        }

        target.setYaw(entityLocation.getYaw());
        target.setPitch(entityLocation.getPitch());
        double distance = entityLocation.distance(target);
        double speed = mob.getEffectiveSpeed(currentTick);

        if (distance <= speed) {
            mob.getEntity().teleport(target);
            mob.setPathIndex(mob.getPathIndex() + 1);
            mob.setRouteProgress(mob.getPathIndex());
            return;
        }

        Location next = entityLocation.clone().add(target.toVector().subtract(entityLocation.toVector()).normalize().multiply(speed));
        mob.getEntity().teleport(next);
        mob.setRouteProgress((mob.getPathIndex() - 1) + (1.0 - (distance / Math.max(0.01, entityLocation.distance(pathManager.getPoint(arena, mob.getPathIndex() - 1))))));
    }

    public long getCurrentTick() {
        return currentTick;
    }
}
