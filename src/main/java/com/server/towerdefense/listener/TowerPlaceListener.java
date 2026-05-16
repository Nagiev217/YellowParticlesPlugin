package com.server.towerdefense.listener;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.economy.EconomyManager;
import com.server.towerdefense.scoreboard.ScoreboardManager;
import com.server.towerdefense.tower.TowerManager;
import com.server.towerdefense.tower.TowerType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TowerPlaceListener implements Listener {
    private final ArenaManager arenaManager;
    private final TowerManager towerManager;
    private final EconomyManager economyManager;
    private final ScoreboardManager scoreboardManager;

    public TowerPlaceListener(ArenaManager arenaManager, TowerManager towerManager, EconomyManager economyManager, ScoreboardManager scoreboardManager) {
        this.arenaManager = arenaManager;
        this.towerManager = towerManager;
        this.economyManager = economyManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
        if (placeLocation.getBlock().getType() != Material.AIR || placeLocation.clone().add(0, 1, 0).getBlock().getType() != Material.AIR) {
            event.getPlayer().sendMessage("Tower space must be clear.");
            return;
        }
        int cost = towerManager.getTowerCost(type);
        if (!economyManager.withdraw(arena, event.getPlayer(), cost)) {
            return;
        }

        towerManager.placeTower(arena, event.getPlayer(), placeLocation, type);
        event.getPlayer().sendMessage("Placed " + type.name() + ".");
        scoreboardManager.update(arena);
    }
}
