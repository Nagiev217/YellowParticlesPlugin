package com.server.towerdefense.economy;

import com.server.towerdefense.arena.Arena;
import com.server.towerdefense.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {
    private final ConfigManager configManager;
    private final Map<String, MatchEconomy> economies = new HashMap<>();

    public EconomyManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void startMatch(Arena arena) {
        MatchEconomy economy = new MatchEconomy(configManager.getConfig().getInt("economy.starting-money", 100));
        for (Player player : arena.getWorld().getPlayers()) {
            economy.registerPlayer(player.getUniqueId());
        }
        economies.put(arena.getId().toLowerCase(), economy);
    }

    public void stopMatch(Arena arena) {
        economies.remove(arena.getId().toLowerCase());
    }

    public int getBalance(Arena arena, UUID playerId) {
        return getEconomy(arena).getBalance(playerId);
    }

    public boolean withdraw(Arena arena, Player player, int amount) {
        MatchEconomy economy = getEconomy(arena);
        economy.registerPlayer(player.getUniqueId());
        if (!economy.withdraw(player.getUniqueId(), amount)) {
            player.sendMessage("Not enough money. Need " + amount + ", you have " + economy.getBalance(player.getUniqueId()) + ".");
            return false;
        }
        player.sendActionBar(Component.text("Money: " + economy.getBalance(player.getUniqueId())));
        return true;
    }

    public void addMoney(Arena arena, UUID playerId, int amount) {
        MatchEconomy economy = getEconomy(arena);
        economy.addMoney(playerId, amount);
        Player player = arena.getWorld().getPlayers().stream()
                .filter(candidate -> candidate.getUniqueId().equals(playerId))
                .findFirst()
                .orElse(null);
        if (player != null) {
            player.sendActionBar(Component.text("+" + amount + " money | Balance: " + economy.getBalance(playerId)));
        }
    }

    public int getSellRefund(int spent) {
        double refundPercent = configManager.getConfig().getDouble("economy.sell-refund-percent", 70.0);
        return (int) Math.floor(spent * Math.max(0.0, Math.min(100.0, refundPercent)) / 100.0);
    }

    private MatchEconomy getEconomy(Arena arena) {
        return economies.computeIfAbsent(arena.getId().toLowerCase(),
                ignored -> new MatchEconomy(configManager.getConfig().getInt("economy.starting-money", 100)));
    }
}
