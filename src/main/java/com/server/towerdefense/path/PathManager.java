package com.server.towerdefense.path;

import com.server.towerdefense.arena.Arena;
import org.bukkit.Location;

import java.util.List;

public class PathManager {
    public boolean hasReachedEnd(Arena arena, int pathIndex) {
        return pathIndex >= arena.getPath().size();
    }

    public Location getPoint(Arena arena, int pathIndex) {
        List<PathPoint> path = arena.getPath();
        if (pathIndex < 0 || pathIndex >= path.size()) {
            return null;
        }
        return path.get(pathIndex).location().clone();
    }
}
