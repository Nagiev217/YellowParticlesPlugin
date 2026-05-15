package com.server.towerdefense.mob;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.path.PathManager;
import com.server.towerdefense.tower.Tower;
import com.server.towerdefense.tower.TowerManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class MobMovementTask extends BukkitRunnable {
    private final ArenaManager arenaManager;
    private final MobManager mobManager;
    private final PathManager pathManager;
    private final TowerManager towerManager;
    private final ConfigManager configManager;
    private long currentTick;

    public MobMovementTask(ArenaManager arenaManager, MobManager mobManager, PathManager pathManager, TowerManager towerManager, ConfigManager configManager) {
        this.arenaManager = arenaManager;
        this.mobManager = mobManager;
        this.pathManager = pathManager;
        this.towerManager = towerManager;
        this.configManager = configManager;
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
        if (attackNearbyTower(arena, mob)) {
            return;
        }

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

        Location previous = pathManager.getPoint(arena, mob.getPathIndex() - 1);
        Location next = entityLocation.clone().add(target.toVector().subtract(entityLocation.toVector()).normalize().multiply(speed));
        mob.getEntity().teleport(next);
        if (previous != null) {
            double segmentLength = previous.distance(target);
            double travelled = previous.distance(next);
            mob.setRouteProgress((mob.getPathIndex() - 1) + Math.min(1.0, travelled / Math.max(0.01, segmentLength)));
        }
    }

    private boolean attackNearbyTower(Arena arena, TDMob mob) {
        double range = configManager.getConfig().getDouble("mobs.tower-attack.range", 1.8);
        double damage = configManager.getConfig().getDouble("mobs.tower-attack.damage", 5.0);
        int attackSpeedTicks = configManager.getConfig().getInt("mobs.tower-attack.attack-speed-ticks", 20);

        Location mobLocation = mob.getEntity().getLocation();
        Tower tower = towerManager.findNearestInRange(arena, mobLocation, range).orElse(null);
        if (tower == null) {
            return false;
        }
        if (!mob.canAttackTower(currentTick, attackSpeedTicks)) {
            return true;
        }

        mob.markTowerAttack(currentTick);
        mob.getEntity().getWorld().spawnParticle(Particle.CRIT, tower.getLocation().add(0.5, 1.0, 0.5), 8, 0.25, 0.25, 0.25, 0.02);
        mob.getEntity().getWorld().playSound(tower.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.45f, 1.2f);
        if (tower.damageTower(damage)) {
            towerManager.removeTower(arena, tower);
            mob.getEntity().getWorld().playSound(mobLocation, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.8f, 1.0f);
        }
        return true;
    }

    public long getCurrentTick() {
        return currentTick;
    }
}
