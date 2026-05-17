package com.server.towerdefense.listener;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.tower.Tower;
import com.server.towerdefense.tower.TowerManager;
import com.server.towerdefense.ui.TowerMenu;
import com.server.towerdefense.visual.ModelDisplayManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class TowerInteractListener implements Listener {
    private final ArenaManager arenaManager;
    private final TowerManager towerManager;
    private final TowerMenu towerMenu;
    private final ModelDisplayManager modelDisplayManager;

    public TowerInteractListener(ArenaManager arenaManager, TowerManager towerManager, TowerMenu towerMenu, ModelDisplayManager modelDisplayManager) {
        this.arenaManager = arenaManager;
        this.towerManager = towerManager;
        this.towerMenu = towerMenu;
        this.modelDisplayManager = modelDisplayManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTowerInteractionEntity(PlayerInteractAtEntityEvent event) {
        modelDisplayManager.readTowerId(event.getRightClicked()).ifPresent(towerId -> {
            Arena arena = arenaManager.findArenaByLocation(event.getRightClicked().getLocation());
            if (arena == null) {
                return;
            }
            Tower tower = towerManager.findById(arena, towerId).orElse(null);
            if (tower == null) {
                return;
            }
            event.setCancelled(true);
            if (!tower.getOwner().equals(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage("This is not your tower.");
                return;
            }
            towerMenu.open(event.getPlayer(), tower);
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTowerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        if (towerManager.readTowerItem(event.getItem()) != null) {
            return;
        }

        Location clickedLocation = event.getClickedBlock().getLocation();
        Arena arena = arenaManager.findArenaByLocation(clickedLocation);
        if (arena == null) {
            return;
        }
        Tower tower = towerManager.findAt(arena, clickedLocation).orElse(null);
        if (tower == null) {
            return;
        }

        event.setCancelled(true);
        if (!tower.getOwner().equals(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage("This is not your tower.");
            return;
        }

        if (event.getItem() != null && event.getItem().getType() == towerManager.getRepairMaterial()) {
            if (tower.isFullHealth()) {
                event.getPlayer().sendMessage("Tower is already fully repaired.");
                return;
            }
            double repaired = towerManager.repairTower(tower);
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            }
            event.getPlayer().sendMessage("Repaired tower by " + Math.ceil(repaired) + " HP.");
            return;
        }

        towerMenu.open(event.getPlayer(), tower);
    }
}
