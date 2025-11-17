package com.hydroline.beacon;

import org.bukkit.plugin.java.JavaPlugin;

public class BeaconPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("HydrolineBeacon enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HydrolineBeacon disabled!");
    }
}
