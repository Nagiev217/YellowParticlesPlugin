package com.server.towerdefense.ui;

import com.server.towerdefense.economy.EconomyManager;
import com.server.towerdefense.tower.Tower;
import com.server.towerdefense.tower.TowerUpgradeService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TowerMenu {
    public static final String TITLE = "Tower Control";
    private final TowerUpgradeService upgradeService;
    private final EconomyManager economyManager;
    private final Map<UUID, Tower> openTowers = new ConcurrentHashMap<>();

    public TowerMenu(TowerUpgradeService upgradeService, EconomyManager economyManager) {
        this.upgradeService = upgradeService;
        this.economyManager = economyManager;
    }

    public void open(Player player, Tower tower) {
        openTowers.put(player.getUniqueId(), tower);
        Inventory inventory = Bukkit.createInventory(player, 27, TITLE);
        inventory.setItem(11, infoItem(tower));
        inventory.setItem(13, upgradeItem(tower));
        inventory.setItem(15, sellItem(tower));
        inventory.setItem(22, targetModeItem(tower));
        player.openInventory(inventory);
    }

    public Tower getOpenTower(Player player) {
        return openTowers.get(player.getUniqueId());
    }

    public void clear(Player player) {
        openTowers.remove(player.getUniqueId());
    }

    private ItemStack infoItem(Tower tower) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Tower Info");
        meta.setLore(List.of(
                "Type: " + tower.getType().name(),
                "Level: " + tower.getLevel(),
                "Damage: " + tower.getDamage(),
                "Range: " + tower.getRange(),
                "Attack Speed: " + tower.getAttackSpeedTicks(),
                "Target: " + tower.getTargetMode().name()
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack upgradeItem(Tower tower) {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (upgradeService.canUpgrade(tower)) {
            meta.setDisplayName("Upgrade");
            meta.setLore(List.of("Cost: " + upgradeService.getNextUpgradeCost(tower)));
        } else {
            meta.setDisplayName("MAX LEVEL");
            meta.setLore(List.of("This tower cannot be upgraded further."));
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack sellItem(Tower tower) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Sell");
        meta.setLore(List.of("Refund: " + economyManager.getSellRefund(tower.getTotalSpent())));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack targetModeItem(Tower tower) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Target Mode");
        List<String> lore = new ArrayList<>();
        lore.add("Current: " + tower.getTargetMode().name());
        lore.add("Click to switch.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
