package com.hydroline.beacon.config;

import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfig {

    private final int port;
    private final String key;
    private final long intervalTimeTicks;
    private final int version;
    private final long nbtCacheTtlMinutes;
    private final boolean mtrWorldScanEnabled;
    private final int mtrWorldScanBatchSize;
    private final String defaultLanguage;

    public PluginConfig(int port,
                        String key,
                        long intervalTimeTicks,
                        int version,
                        long nbtCacheTtlMinutes,
                        boolean mtrWorldScanEnabled,
                        int mtrWorldScanBatchSize,
                        String defaultLanguage) {
        this.port = port;
        this.key = key;
        this.intervalTimeTicks = intervalTimeTicks;
        this.version = version;
        this.nbtCacheTtlMinutes = nbtCacheTtlMinutes;
        this.mtrWorldScanEnabled = mtrWorldScanEnabled;
        this.mtrWorldScanBatchSize = mtrWorldScanBatchSize;
        this.defaultLanguage = defaultLanguage;
    }

    public static PluginConfig fromConfig(FileConfiguration config) {
        int port = config.getInt("port");
        String key = config.getString("key", "");
        long intervalTimeTicks = config.getLong("interval_time");
        int version = config.getInt("version");
        long nbtCacheTtlMinutes = config.getLong("nbt_cache_ttl_minutes", 10L);
        boolean mtrWorldScanEnabled = config.getBoolean("mtr_world_scan_enabled", true);
        int mtrWorldScanBatchSize = config.getInt("mtr_world_scan_batch_size", 16);
        if (mtrWorldScanBatchSize <= 0) {
            mtrWorldScanBatchSize = 16;
        }
        String defaultLanguage = config.getString("default_language", "zh_cn");
        if (defaultLanguage == null || defaultLanguage.trim().isEmpty()) {
            defaultLanguage = "zh_cn";
        }
        return new PluginConfig(
                port,
                key,
                intervalTimeTicks,
                version,
                nbtCacheTtlMinutes,
                mtrWorldScanEnabled,
                mtrWorldScanBatchSize,
                defaultLanguage
        );
    }

    public int getPort() {
        return port;
    }

    public String getKey() {
        return key;
    }

    public long getIntervalTimeTicks() {
        return intervalTimeTicks;
    }

    public int getVersion() {
        return version;
    }

    public long getNbtCacheTtlMinutes() {
        return nbtCacheTtlMinutes;
    }

    public boolean isMtrWorldScanEnabled() {
        return mtrWorldScanEnabled;
    }

    public int getMtrWorldScanBatchSize() {
        return mtrWorldScanBatchSize;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}
