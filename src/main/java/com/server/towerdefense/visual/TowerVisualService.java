package com.server.towerdefense.visual;

import com.server.towerdefense.tower.Tower;
import com.server.towerdefense.tower.TowerType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class TowerVisualService {
    public ArmorStand buildTower(Location location, TowerType type) {
        Block base = location.getBlock();
        Block top = location.clone().add(0, 1, 0).getBlock();
        base.setType(type.getBaseMaterial());
        top.setType(type.getTopMaterial());

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0.5, 2.25, 0.5), EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setMarker(true);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setCustomNameVisible(true);
        return armorStand;
    }

    public void removeTower(Tower tower) {
        Location location = tower.getLocation();
        location.getBlock().setType(org.bukkit.Material.AIR);
        location.clone().add(0, 1, 0).getBlock().setType(org.bukkit.Material.AIR);
        tower.removeHealthDisplay();
    }
}
