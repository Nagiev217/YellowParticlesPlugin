package com.server.towerdefense.base;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.mob.MobType;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BaseManager {
    private final ConfigManager configManager;
    private final Map<String, Base> bases = new HashMap<>();
    private final Map<String, BossBar> bossBars = new HashMap<>();

    public BaseManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void startMatch(Arena arena) {
        Base base = new Base(arena.getBaseLocation(), configManager.getConfig().getInt("base.max-hp", 100));
        bases.put(arena.getId().toLowerCase(), base);
        updateBossBar(arena);
    }

    public void stopMatch(Arena arena) {
        bases.remove(arena.getId().toLowerCase());
        BossBar bossBar = bossBars.remove(arena.getId().toLowerCase());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public Base getBase(Arena arena) {
        return bases.computeIfAbsent(arena.getId().toLowerCase(),
                ignored -> new Base(arena.getBaseLocation(), configManager.getConfig().getInt("base.max-hp", 100)));
    }

    public boolean damageBase(Arena arena, MobType mobType) {
        int damage = configManager.getConfig().getInt("mobs." + mobType.getConfigKey() + ".base-damage", 1);
        Base base = getBase(arena);
        base.damage(damage);
        updateBossBar(arena);
        return base.isDestroyed();
    }

    public void updateBossBar(Arena arena) {
        Base base = getBase(arena);
        BossBar bossBar = bossBars.computeIfAbsent(arena.getId().toLowerCase(),
                ignored -> Bukkit.createBossBar("Base HP", BarColor.GREEN, BarStyle.SOLID));
        for (Player player : arena.getWorld().getPlayers()) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
        double progress = base.getMaxHp() <= 0 ? 0.0 : base.getCurrentHp() / (double) base.getMaxHp();
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
        bossBar.setTitle("Base HP: " + base.getCurrentHp() + "/" + base.getMaxHp());
        if (progress <= 0.30) {
            bossBar.setColor(BarColor.RED);
        } else if (progress <= 0.60) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.GREEN);
        }
    }
}
