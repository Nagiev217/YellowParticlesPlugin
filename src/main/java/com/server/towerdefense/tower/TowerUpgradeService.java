package com.server.towerdefense.tower;

import com.server.towerdefense.config.ConfigManager;
import com.server.towerdefense.visual.TowerVisualService;
import org.bukkit.configuration.ConfigurationSection;

public class TowerUpgradeService {
    private final ConfigManager configManager;
    private TowerVisualService towerVisualService;

    public TowerUpgradeService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void setTowerVisualService(TowerVisualService towerVisualService) {
        this.towerVisualService = towerVisualService;
    }

    public TowerUpgradeData getData(TowerType type, int level) {
        String root = "towers." + type.getConfigKey() + ".levels." + level + ".";
        return new TowerUpgradeData(
                level,
                configManager.getConfig().getDouble(root + "damage", configManager.getConfig().getDouble("towers." + type.getConfigKey() + ".damage")),
                configManager.getConfig().getDouble(root + "range", configManager.getConfig().getDouble("towers." + type.getConfigKey() + ".range")),
                configManager.getConfig().getInt(root + "attack-speed-ticks", configManager.getConfig().getInt("towers." + type.getConfigKey() + ".attack-speed-ticks")),
                configManager.getConfig().getInt(root + "upgrade-cost", 0),
                configManager.getConfig().getDouble(root + "splash-radius", configManager.getConfig().getDouble("towers." + type.getConfigKey() + ".splash-radius", 0.0)),
                configManager.getConfig().getDouble(root + "slow-percent", configManager.getConfig().getDouble("towers." + type.getConfigKey() + ".slow-percent", 0.0)),
                configManager.getConfig().getInt(root + "slow-duration-ticks", configManager.getConfig().getInt("towers." + type.getConfigKey() + ".slow-duration-ticks", 0))
        );
    }

    public int getMaxLevel(TowerType type) {
        ConfigurationSection section = configManager.getConfig().getConfigurationSection("towers." + type.getConfigKey() + ".levels");
        return section == null ? 3 : section.getKeys(false).stream()
                .mapToInt(key -> {
                    try {
                        return Integer.parseInt(key);
                    } catch (NumberFormatException exception) {
                        return 1;
                    }
                })
                .max()
                .orElse(3);
    }

    public boolean canUpgrade(Tower tower) {
        return tower.getLevel() < getMaxLevel(tower.getType());
    }

    public int getNextUpgradeCost(Tower tower) {
        if (!canUpgrade(tower)) {
            return 0;
        }
        return getData(tower.getType(), tower.getLevel()).getUpgradeCost();
    }

    public void applyLevel(Tower tower, int level) {
        tower.applyUpgradeData(getData(tower.getType(), level));
        if (towerVisualService != null) {
            towerVisualService.updateTowerModel(tower);
        }
    }
}
