package com.server.towerdefense.mob;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.visual.MobModelData;
import com.server.towerdefense.visual.MobVisualService;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MobManager {
    public static final String TD_MOB_METADATA = "towerdefense_mob";

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final MobVisualService mobVisualService;

    public MobManager(JavaPlugin plugin, ConfigManager configManager, MobVisualService mobVisualService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.mobVisualService = mobVisualService;
    }

    public TDMob spawnMob(Arena arena, MobType type) {
        String root = "mobs." + type.getConfigKey() + ".";
        double maxHp = configManager.getConfig().getDouble(root + "max-hp");
        double speed = configManager.getConfig().getDouble(root + "speed");
        int reward = configManager.getConfig().getInt(root + "reward");

        MobModelData modelData = mobVisualService.getModelData(type);
        LivingEntity entity = (LivingEntity) arena.getWorld().spawnEntity(arena.getMobSpawn(), modelData.getBaseEntity());
        entity.setMetadata(TD_MOB_METADATA, new FixedMetadataValue(plugin, arena.getId()));
        entity.setRemoveWhenFarAway(false);
        entity.setCanPickupItems(false);
        entity.setCustomNameVisible(true);
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.max(1.0, maxHp));
        }
        entity.setHealth(Math.max(1.0, maxHp));
        if (entity instanceof Mob mob) {
            mob.setAI(false);
            mob.setAware(false);
            mob.setTarget(null);
        }

        TDMob tdMob = new TDMob(UUID.randomUUID(), entity, type, maxHp, speed, reward);
        entity.setCustomName(tdMob.formatName());
        mobVisualService.applyVisual(tdMob);
        arena.getActiveMobs().add(tdMob);
        return tdMob;
    }

    public void killMob(Arena arena, TDMob mob) {
        arena.getActiveMobs().remove(mob);
        mobVisualService.removeMobVisual(mob);
        mob.getEntity().remove();
    }

    public void leakMob(Arena arena, TDMob mob) {
        arena.getActiveMobs().remove(mob);
        mobVisualService.removeMobVisual(mob);
        mob.getEntity().remove();
        arena.getWorld().strikeLightningEffect(arena.getBaseLocation());
    }

    public void removeAll(Arena arena) {
        List<TDMob> copy = new ArrayList<>(arena.getActiveMobs());
        for (TDMob mob : copy) {
            mobVisualService.removeMobVisual(mob);
            mob.getEntity().remove();
        }
        arena.getActiveMobs().clear();
    }

    public List<TDMob> getLivingMobs(Arena arena) {
        arena.getActiveMobs().removeIf(mob -> mob.getEntity().isDead() || !mob.getEntity().isValid());
        return arena.getActiveMobs();
    }

    public MobVisualService getMobVisualService() {
        return mobVisualService;
    }
}
