package com.example.yellowparticles;

import org.bukkit.plugin.java.JavaPlugin;

public class YellowParticlesPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("YellowParticlesPlugin успешно загружен!");
        MochaCommand mochaCommand = new MochaCommand(this);
        getCommand("mocha").setExecutor(mochaCommand);
    }

    @Override
    public void onDisable() {
        getLogger().info("YellowParticlesPlugin выгружен.");
    }
}
