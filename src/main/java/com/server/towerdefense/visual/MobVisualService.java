package com.server.towerdefense.visual;

import com.server.towerdefense.mob.MobType;
import org.bukkit.entity.LivingEntity;

public class MobVisualService {
    public void applyVisual(LivingEntity entity, MobType type) {
        entity.setCustomNameVisible(true);
    }
}
