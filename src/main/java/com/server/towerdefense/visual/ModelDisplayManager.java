package com.server.towerdefense.visual;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

public class ModelDisplayManager {
    private final NamespacedKey towerIdKey;
    private final NamespacedKey mobIdKey;
    private final NamespacedKey testModelKey;

    public ModelDisplayManager(JavaPlugin plugin) {
        this.towerIdKey = new NamespacedKey(plugin, "tower_id");
        this.mobIdKey = new NamespacedKey(plugin, "mob_id");
        this.testModelKey = new NamespacedKey(plugin, "test_model");
    }

    public ItemDisplay spawnItemDisplay(Location location, Material material, int customModelData, float scale, float yawDegrees) {
        ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);

        display.setItemStack(createModelItem(material, customModelData));
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        display.setBillboard(Display.Billboard.FIXED);

        applyTransform(display, scale, yawDegrees, 0.0f);
        return display;
    }

    public Interaction spawnInteraction(Location location, float width, float height, UUID towerId) {
        Interaction interaction = (Interaction) location.getWorld().spawnEntity(location, EntityType.INTERACTION);
        interaction.setInteractionWidth(width);
        interaction.setInteractionHeight(height);
        interaction.getPersistentDataContainer().set(towerIdKey, PersistentDataType.STRING, towerId.toString());
        return interaction;
    }

    public ItemStack createModelItem(Material material, int customModelData) {
        ItemStack item = new ItemStack(material == null ? Material.PAPER : material);
        ItemMeta meta = item.getItemMeta();

        meta.setCustomModelData(customModelData);

        item.setItemMeta(meta);
        return item;
    }

    public void tagTowerDisplay(Entity entity, UUID towerId) {
        entity.getPersistentDataContainer().set(towerIdKey, PersistentDataType.STRING, towerId.toString());
    }

    public void tagMobDisplay(Entity entity, UUID mobId) {
        entity.getPersistentDataContainer().set(mobIdKey, PersistentDataType.STRING, mobId.toString());
    }

    public void tagTestModel(Entity entity) {
        entity.getPersistentDataContainer().set(testModelKey, PersistentDataType.INTEGER, 1);
    }

    public boolean isTestModel(Entity entity) {
        return entity.getPersistentDataContainer().has(testModelKey, PersistentDataType.INTEGER);
    }

    public Optional<UUID> readTowerId(Entity entity) {
        String raw = entity.getPersistentDataContainer().get(towerIdKey, PersistentDataType.STRING);
        if (raw == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(raw));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public void updateItemDisplay(ItemDisplay display, Material material, int customModelData, float scale, float yawDegrees) {
        if (display == null || !display.isValid()) {
            return;
        }

        display.setItemStack(createModelItem(material, customModelData));
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);

        applyTransform(display, scale, yawDegrees, 0.0f);
    }

    public void applyTransform(ItemDisplay display, float scale, float yawDegrees, float pitchDegrees) {
        float yawRadians = (float) Math.toRadians(yawDegrees);
        float pitchRadians = (float) Math.toRadians(pitchDegrees);

        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(yawRadians, 0, 1, 0),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f(pitchRadians, 1, 0, 0)
        ));
    }
}
