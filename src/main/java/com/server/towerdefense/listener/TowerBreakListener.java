package com.server.towerdefense.listener;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.arena.ArenaManager;
import com.server.towerdefense.tower.TowerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class TowerBreakListener implements Listener {
    private final ArenaManager arenaManager;
    private final TowerManager towerManager;

    public TowerBreakListener(ArenaManager arenaManager, TowerManager towerManager) {
        this.arenaManager = arenaManager;
        this.towerManager = towerManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Arena arena = arenaManager.findArenaByLocation(event.getBlock().getLocation());
        if (arena != null && towerManager.findAt(arena, event.getBlock().getLocation()).isPresent()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Use the tower menu to sell towers.");
        }
    }
}
