package com.server.towerdefense.listener;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.tower.TowerManager;
import com.server.towerdefense.tower.TowerType;
import org.bukkit.Location;
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

        towerManager.placeTower(arena, event.getPlayer(), placeLocation, type);
        event.getPlayer().sendMessage("Placed " + type.name() + ".");
    }
}
