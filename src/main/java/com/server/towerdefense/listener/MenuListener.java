package com.server.towerdefense.listener;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.economy.EconomyManager;
import com.server.towerdefense.scoreboard.ScoreboardManager;
import com.server.towerdefense.tower.TargetMode;
import com.server.towerdefense.tower.Tower;
import com.server.towerdefense.tower.TowerManager;
import com.server.towerdefense.tower.TowerUpgradeService;
import com.server.towerdefense.ui.TowerMenu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener implements Listener {
    private final ArenaManager arenaManager;
    private final TowerManager towerManager;
    private final TowerUpgradeService upgradeService;
    private final EconomyManager economyManager;
    private final ScoreboardManager scoreboardManager;
    private final TowerMenu towerMenu;

    public MenuListener(ArenaManager arenaManager, TowerManager towerManager, TowerUpgradeService upgradeService,
                        EconomyManager economyManager, ScoreboardManager scoreboardManager, TowerMenu towerMenu) {
        this.arenaManager = arenaManager;
        this.towerManager = towerManager;
        this.upgradeService = upgradeService;
        this.economyManager = economyManager;
        this.scoreboardManager = scoreboardManager;
        this.towerMenu = towerMenu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!TowerMenu.TITLE.equals(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Tower tower = towerMenu.getOpenTower(player);
        Arena arena = arenaManager.findArenaByLocation(player.getLocation());
        if (tower == null || arena == null) {
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() == 13) {
            upgrade(player, arena, tower);
        } else if (event.getRawSlot() == 15) {
            sell(player, arena, tower);
        } else if (event.getRawSlot() == 22) {
            tower.setTargetMode(nextMode(tower.getTargetMode()));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            towerMenu.open(player, tower);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && TowerMenu.TITLE.equals(event.getView().getTitle())) {
            towerMenu.clear(player);
        }
    }

    private void upgrade(Player player, Arena arena, Tower tower) {
        if (!upgradeService.canUpgrade(tower)) {
            player.sendMessage("Tower is already max level.");
            return;
        }
        int cost = upgradeService.getNextUpgradeCost(tower);
        if (!economyManager.withdraw(arena, player, cost)) {
            return;
        }
        upgradeService.applyLevel(tower, tower.getLevel() + 1);
        tower.addSpent(cost);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.6f, 1.4f);
        scoreboardManager.update(arena);
        towerMenu.open(player, tower);
    }

    private void sell(Player player, Arena arena, Tower tower) {
        int refund = economyManager.getSellRefund(tower.getTotalSpent());
        economyManager.addMoney(arena, player.getUniqueId(), refund);
        towerManager.removeTower(arena, tower);
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.2f);
        scoreboardManager.update(arena);
    }

    private TargetMode nextMode(TargetMode current) {
        return switch (current) {
            case FIRST -> TargetMode.LAST;
            case LAST -> TargetMode.STRONGEST;
            case STRONGEST -> TargetMode.NEAREST;
            case NEAREST -> TargetMode.FIRST;
        };
    }
}
