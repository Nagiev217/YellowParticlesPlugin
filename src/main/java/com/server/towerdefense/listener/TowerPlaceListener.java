package com.server.towerdefense.listener;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.tower.Tower;
import com.server.towerdefense.tower.TowerManager;
import com.server.towerdefense.tower.TowerType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TowerPlaceListener implements Listener {
    private final ArenaManager arenaManager;
    private final TowerManager towerManager;

    public TowerPlaceListener(ArenaManager arenaManager, TowerManager towerManager) {
        this.arenaManager = arenaManager;
        this.towerManager = towerManager;
    }

    @EventHandler
    public void onPlaceTower(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        if (tryRepairTower(event)) {
            return;
        }

        TowerType type = towerManager.readTowerItem(event.getItem());
        if (type == null) {
            return;
        }

        event.setCancelled(true);
        Location placeLocation = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
        Arena arena = arenaManager.findArenaByLocation(placeLocation);
        if (arena == null) {
            event.getPlayer().sendMessage("No running TD arena in this world.");
            return;
        }
        if (towerManager.findAt(arena, placeLocation).isPresent()) {
            event.getPlayer().sendMessage("There is already a tower here.");
            return;
        }
        if (placeLocation.getBlock().getType() != Material.AIR || placeLocation.clone().add(0, 1, 0).getBlock().getType() != Material.AIR) {
            event.getPlayer().sendMessage("Tower space must be clear.");
            return;
        }

        towerManager.placeTower(arena, event.getPlayer(), placeLocation, type);
        event.getPlayer().sendMessage("Placed " + type.name() + ".");
    }

    private boolean tryRepairTower(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != towerManager.getRepairMaterial()) {
            return false;
        }

        Location clickedLocation = event.getClickedBlock().getLocation();
        Arena arena = arenaManager.findArenaByLocation(clickedLocation);
        if (arena == null) {
            return false;
        }

        Tower tower = towerManager.findAt(arena, clickedLocation).orElse(null);
        if (tower == null) {
            return false;
        }

        event.setCancelled(true);
        if (tower.isFullHealth()) {
            event.getPlayer().sendMessage("Tower is already fully repaired.");
            return true;
        }

        double repaired = towerManager.repairTower(tower);
        if (repaired <= 0.0) {
            event.getPlayer().sendMessage("Tower could not be repaired.");
            return true;
        }

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.getItem().setAmount(event.getItem().getAmount() - 1);
        }
        event.getPlayer().sendMessage("Repaired tower by " + Math.ceil(repaired) + " HP.");
        return true;
    }
}
