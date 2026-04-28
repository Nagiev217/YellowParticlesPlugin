package com.example.yellowparticles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MochaCommand implements CommandExecutor {

    private final YellowParticlesPlugin plugin;

    private static final Particle.DustOptions YELLOW_DUST =
            new Particle.DustOptions(Color.fromRGB(255, 220, 0), 1.2f);

    private static final int PARTICLE_INTERVAL_TICKS = 3;
    private static final int EFFECT_DURATION_TICKS = 200;

    public MochaCommand(YellowParticlesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду может использовать только игрок.");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage("§e✨ Эффект капель активирован на 10 секунд!");
        startDripEffect(player);
        return true;
    }

    private void startDripEffect(Player player) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= EFFECT_DURATION_TICKS || !player.isOnline()) {
                    this.cancel();
                    if (player.isOnline()) {
                        player.sendMessage("§7Эффект капель завершён.");
                    }
                    return;
                }

                Location loc = player.getLocation().clone().subtract(0, 0.3, 0);
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();

                for (int i = 0; i < 4; i++) {
                    double offsetX = (Math.random() - 0.5) * 0.6;
                    double offsetZ = (Math.random() - 0.5) * 0.6;
                    double dropY = y - Math.random() * 0.2;

                    Location dropLoc = new Location(loc.getWorld(), x + offsetX, dropY, z + offsetZ);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, dropLoc, 1, 0, 0, 0, 0, YELLOW_DUST);
                }

                if (ticks % 6 == 0) {
                    for (int i = 0; i < 2; i++) {
                        double offsetX = (Math.random() - 0.5) * 0.4;
                        double offsetZ = (Math.random() - 0.5) * 0.4;
                        Location splashLoc = new Location(loc.getWorld(), x + offsetX, y + 0.05, z + offsetZ);
                        loc.getWorld().spawnParticle(Particle.REDSTONE, splashLoc, 1, 0.05, 0.02, 0.05, 0, YELLOW_DUST);
                    }
                }

                ticks += PARTICLE_INTERVAL_TICKS;
            }
        }.runTaskTimer(plugin, 0L, PARTICLE_INTERVAL_TICKS);
    }
}
