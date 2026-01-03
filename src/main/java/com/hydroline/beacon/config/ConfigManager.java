package com.hydroline.beacon.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.SecureRandom;

public class ConfigManager {

    private static final int KEY_LENGTH = 64;
    private static final String KEY_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final JavaPlugin plugin;
    private PluginConfig currentConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        FileConfiguration cfg = plugin.getConfig();

        String key = cfg.getString("key", "");
        if (key == null || key.isEmpty()) {
            key = generateRandomKey();
            cfg.set("key", key);
            plugin.saveConfig();
        }

        int port = cfg.getInt("port");
        if (port <= 0) {
            port = 48080;
            cfg.set("port", port);
        }

        long intervalTicks = cfg.getLong("interval_time");
        if (intervalTicks <= 0) {
            intervalTicks = 200L;
            cfg.set("interval_time", intervalTicks);
        }

        int version = cfg.getInt("version");
        if (version <= 0) {
            version = 1;
            cfg.set("version", version);
        }

        long nbtCacheTtlMinutes = cfg.getLong("nbt_cache_ttl_minutes");
        if (nbtCacheTtlMinutes <= 0) {
            nbtCacheTtlMinutes = 10L;
            cfg.set("nbt_cache_ttl_minutes", nbtCacheTtlMinutes);
        }

        if (!cfg.contains("mtr_world_scan_enabled")) {
            cfg.set("mtr_world_scan_enabled", true);
        }
        boolean mtrWorldScanEnabled = cfg.getBoolean("mtr_world_scan_enabled");
        long mtrWorldScanBatchSize = cfg.getLong("mtr_world_scan_batch_size");
        if (mtrWorldScanBatchSize <= 0) {
            mtrWorldScanBatchSize = 16;
            cfg.set("mtr_world_scan_batch_size", mtrWorldScanBatchSize);
        }

        String defaultLanguage = cfg.getString("default_language", "zh_cn");
        if (defaultLanguage == null || defaultLanguage.trim().isEmpty()) {
            defaultLanguage = "zh_cn";
            cfg.set("default_language", defaultLanguage);
        }

        plugin.saveConfig();
        currentConfig = new PluginConfig(
                port,
                key,
                intervalTicks,
                version,
                nbtCacheTtlMinutes,
                mtrWorldScanEnabled,
                (int) mtrWorldScanBatchSize,
                defaultLanguage
        );
    }

    public PluginConfig getCurrentConfig() {
        return currentConfig;
    }

    private String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = random.nextInt(KEY_CHARSET.length());
            sb.append(KEY_CHARSET.charAt(index));
        }
        return sb.toString();
    }
}
