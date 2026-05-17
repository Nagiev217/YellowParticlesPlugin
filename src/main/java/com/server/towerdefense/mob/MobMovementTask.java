package com.server.towerdefense.mob;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.animation.MobAnimationService;
import com.server.towerdefense.base.BaseManager;
import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.path.PathManager;
import com.server.towerdefense.scoreboard.ScoreboardManager;
import com.server.towerdefense.tower.Tower;
import com.server.towerdefense.tower.TowerManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class MobMovementTask extends BukkitRunnable {
    private final ArenaManager arenaManager;
    private final MobManager mobManager;
    private final PathManager pathManager;
    private final TowerManager towerManager;
    private final ConfigManager configManager;
    private final BaseManager baseManager;
    private final ScoreboardManager scoreboardManager;
    private final MobAnimationService mobAnimationService;
    private long currentTick;

    public MobMovementTask(ArenaManager arenaManager, MobManager mobManager, PathManager pathManager, TowerManager towerManager,
                           ConfigManager configManager, BaseManager baseManager, ScoreboardManager scoreboardManager,
                           MobAnimationService mobAnimationService) {
        this.arenaManager = arenaManager;
        this.mobManager = mobManager;
        this.pathManager = pathManager;
        this.towerManager = towerManager;
        this.configManager = configManager;
        this.baseManager = baseManager;
        this.scoreboardManager = scoreboardManager;
        this.mobAnimationService = mobAnimationService;
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
                mobManager.getMobVisualService().updateMobVisual(mob);
                mobAnimationService.animateMove(mob, currentTick);
            }
        }
    }

    private void moveMob(Arena arena, TDMob mob) {
        if (attackNearbyTower(arena, mob)) {
            return;
        }

        if (pathManager.hasReachedEnd(arena, mob.getPathIndex())) {
            mobManager.leakMob(arena, mob);
            if (baseManager.damageBase(arena, mob.getType())) {
                arenaManager.defeatArena(arena);
                return;
            }
            scoreboardManager.update(arena);
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
        if (mob.hasAttemptedTowerAttack()) {
            return false;
        }

        double range = configManager.getConfig().getDouble("mobs.tower-attack.range", 1.8);
        double damage = configManager.getConfig().getDouble("mobs.tower-attack.damage", 5.0);
        double chancePercent = configManager.getConfig().getDouble("mobs.tower-attack.chance-percent", 10.0);

        Location mobLocation = mob.getEntity().getLocation();
        Tower tower = towerManager.findNearestInRange(arena, mobLocation, range).orElse(null);
        if (tower == null) {
            return false;
        }

        mob.markTowerAttackAttempted();
        double clampedChance = Math.max(0.0, Math.min(100.0, chancePercent));
        if (ThreadLocalRandom.current().nextDouble(100.0) >= clampedChance) {
            return false;
        }

        mob.getEntity().getWorld().spawnParticle(Particle.CRIT, tower.getLocation().add(0.5, 1.0, 0.5), 8, 0.25, 0.25, 0.25, 0.02);
        mob.getEntity().getWorld().playSound(tower.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.45f, 1.2f);
        if (tower.damageTower(damage)) {
            towerManager.removeTower(arena, tower);
            mob.getEntity().getWorld().playSound(mobLocation, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.8f, 1.0f);
        }
        return false;
    }

    public long getCurrentTick() {
        return currentTick;
    }
}
