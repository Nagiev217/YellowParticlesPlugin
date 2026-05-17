package com.server.towerdefense.visual;

import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public record TowerVisualEntities(ArmorStand healthDisplay, UUID displayId, UUID interactionId) {
}
