package com.server.towerdefense.tower;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.economy.EconomyManager;
import com.server.towerdefense.mob.MobManager;
import com.server.towerdefense.mob.TDMob;
import com.server.towerdefense.scoreboard.ScoreboardManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TowerAttackTask extends BukkitRunnable {
    private final ArenaManager arenaManager;
    private final MobManager mobManager;
    private final EconomyManager economyManager;
    private final ScoreboardManager scoreboardManager;
    private long currentTick;

    public TowerAttackTask(ArenaManager arenaManager, MobManager mobManager, EconomyManager economyManager, ScoreboardManager scoreboardManager) {
        this.arenaManager = arenaManager;
        this.mobManager = mobManager;
        this.economyManager = economyManager;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public void run() {
        currentTick++;
        for (Arena arena : arenaManager.getArenas()) {
            if (!arena.isRunning()) {
                continue;
            }
            for (Tower tower : arena.getActiveTowers()) {
                if (!tower.canAttack(currentTick)) {
                    continue;
                }
                Optional<TDMob> target = findTarget(arena, tower);
                target.ifPresent(tdMob -> attack(arena, tower, tdMob));
            }
        }
    }

    private Optional<TDMob> findTarget(Arena arena, Tower tower) {
        Location towerLocation = tower.getLocation().add(0.5, 1.0, 0.5);
        List<TDMob> inRange = mobManager.getLivingMobs(arena).stream()
                .filter(mob -> mob.getEntity().getWorld().equals(towerLocation.getWorld()))
                .filter(mob -> mob.getEntity().getLocation().distanceSquared(towerLocation) <= tower.getRange() * tower.getRange())
                .toList();

        Comparator<TDMob> comparator = switch (tower.getTargetMode()) {
            case FIRST -> Comparator.comparingDouble(TDMob::getRouteProgress).reversed();
            case LAST -> Comparator.comparingDouble(TDMob::getRouteProgress);
            case STRONGEST -> Comparator.comparingDouble(TDMob::getHp).reversed();
            case NEAREST -> Comparator.comparingDouble(mob -> mob.getEntity().getLocation().distanceSquared(towerLocation));
        };
        return inRange.stream().min(comparator);
    }

    private void attack(Arena arena, Tower tower, TDMob target) {
        tower.markAttack(currentTick);
        switch (tower.getType()) {
            case ARCHER_TOWER -> damageSingle(arena, tower, target, Particle.CRIT, Sound.ENTITY_ARROW_SHOOT);
            case CANNON_TOWER -> damageSplash(arena, tower, target);
            case ICE_TOWER -> damageIce(arena, tower, target);
        }
    }

    private void damageSingle(Arena arena, Tower tower, TDMob target, Particle particle, Sound sound) {
        Location effectLocation = target.getEntity().getLocation().add(0, 1, 0);
        target.getEntity().getWorld().spawnParticle(particle, effectLocation, 8, 0.2, 0.2, 0.2, 0.02);
        target.getEntity().getWorld().playSound(effectLocation, sound, 0.5f, 1.2f);
        if (target.damage(tower.getDamage())) {
            mobManager.killMob(arena, target);
            economyManager.addMoney(arena, tower.getOwner(), target.getReward());
            scoreboardManager.update(arena);
        }
    }

    private void damageSplash(Arena arena, Tower tower, TDMob target) {
        double splashRadius = tower.getSplashRadius();
        Location center = target.getEntity().getLocation();
        center.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, center, 20, 0.8, 0.4, 0.8, 0.04);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.4f);

        for (TDMob mob : mobManager.getLivingMobs(arena).toArray(new TDMob[0])) {
            if (!mob.getEntity().getWorld().equals(center.getWorld())) {
                continue;
            }
            if (mob.getEntity().getLocation().distanceSquared(center) <= splashRadius * splashRadius && mob.damage(tower.getDamage())) {
                mobManager.killMob(arena, mob);
                economyManager.addMoney(arena, tower.getOwner(), mob.getReward());
                scoreboardManager.update(arena);
            }
        }
    }

    private void damageIce(Arena arena, Tower tower, TDMob target) {
        target.applySlow(tower.getSlowPercent(), tower.getSlowDurationTicks(), currentTick);
        damageSingle(arena, tower, target, Particle.SNOWFLAKE, Sound.BLOCK_GLASS_BREAK);
    }
}
