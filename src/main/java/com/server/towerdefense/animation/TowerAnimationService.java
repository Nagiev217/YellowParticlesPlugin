package com.server.towerdefense.animation;

import com.server.towerdefense.tower.Tower;
import com.server.towerdefense.tower.TowerType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;

public class TowerAnimationService {
    public void playAttack(Tower tower, Location targetLocation) {
        Location towerLocation = tower.getLocation().add(0.5, 1.1, 0.5);
        if (tower.getDisplayEntityId() != null) {
            Entity entity = towerLocation.getWorld().getEntity(tower.getDisplayEntityId());
            if (entity instanceof ItemDisplay display && display.isValid()) {
                Location look = display.getLocation();
                double dx = targetLocation.getX() - look.getX();
                double dz = targetLocation.getZ() - look.getZ();
                look.setYaw((float) Math.toDegrees(Math.atan2(-dx, dz)));
                display.teleport(look);
            }
        }

        if (tower.getType() == TowerType.ARCHER_TOWER) {
            towerLocation.getWorld().spawnParticle(Particle.CRIT, towerLocation, 8, 0.2, 0.2, 0.2, 0.01);
            towerLocation.getWorld().playSound(towerLocation, Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.2f);
        } else if (tower.getType() == TowerType.CANNON_TOWER) {
            targetLocation.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, targetLocation, 18, 0.6, 0.3, 0.6, 0.03);
            targetLocation.getWorld().playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.45f, 1.5f);
        } else {
            targetLocation.getWorld().spawnParticle(Particle.SNOWFLAKE, targetLocation, 14, 0.4, 0.4, 0.4, 0.02);
            targetLocation.getWorld().playSound(targetLocation, Sound.BLOCK_GLASS_BREAK, 0.4f, 1.4f);
        }
    }
}
