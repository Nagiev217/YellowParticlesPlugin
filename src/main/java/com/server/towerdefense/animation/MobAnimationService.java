package com.server.towerdefense.animation;

import com.server.towerdefense.mob.TDMob;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;

public class MobAnimationService {
    public void animateMove(TDMob mob, long tick) {
        if (mob.getDisplayEntityId() == null) {
            return;
        }
        Entity entity = mob.getEntity().getWorld().getEntity(mob.getDisplayEntityId());
        if (!(entity instanceof ItemDisplay display) || !display.isValid()) {
            return;
        }
        Location location = display.getLocation();
        location.setYaw(location.getYaw() + 3.0f);
        location.setY(location.getY() + Math.sin(tick / 6.0) * 0.015);
        display.teleport(location);
    }

    public void playDamage(TDMob mob) {
        mob.getEntity().getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, mob.getEntity().getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.02);
    }

    public void playDeath(TDMob mob) {
        mob.getEntity().getWorld().spawnParticle(Particle.CLOUD, mob.getEntity().getLocation().add(0, 0.5, 0), 12, 0.3, 0.3, 0.3, 0.03);
    }
}
