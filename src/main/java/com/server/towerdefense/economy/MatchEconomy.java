package com.server.towerdefense.economy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MatchEconomy {
    private final int startingMoney;
    private final Map<UUID, Integer> balances = new HashMap<>();

    public MatchEconomy(int startingMoney) {
        this.startingMoney = startingMoney;
    }

    public void registerPlayer(UUID playerId) {
        balances.put(playerId, startingMoney);
    }

    public int getBalance(UUID playerId) {
        return balances.getOrDefault(playerId, startingMoney);
    }

    public void addMoney(UUID playerId, int amount) {
        balances.put(playerId, Math.max(0, getBalance(playerId) + amount));
    }

    public boolean withdraw(UUID playerId, int amount) {
        int balance = getBalance(playerId);
        if (balance < amount) {
            return false;
        }
        balances.put(playerId, balance - amount);
        return true;
    }
}
