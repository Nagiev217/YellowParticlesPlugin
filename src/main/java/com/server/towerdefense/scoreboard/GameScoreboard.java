package com.server.towerdefense.scoreboard;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public record GameScoreboard(Scoreboard scoreboard, Objective objective) {
}
