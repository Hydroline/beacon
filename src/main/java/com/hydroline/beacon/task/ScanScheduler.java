package com.hydroline.beacon.task;

import com.hydroline.beacon.BeaconPlugin;
import com.hydroline.beacon.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class ScanScheduler {

    private final BeaconPlugin plugin;
    private BukkitTask advancementsAndStatsTask;
    private BukkitTask mtrLogsTask;
    private BukkitTask nbtIdentityTask;
    private BukkitTask mtrWorldTask;

    public ScanScheduler(BeaconPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        PluginConfig cfg = plugin.getConfigManager().getCurrentConfig();
        long interval = cfg.getIntervalTimeTicks();
        if (interval <= 0L) {
            interval = 200L;
        }

        advancementsAndStatsTask = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> {
                    new AdvancementsAndStatsScanner(plugin).scanOnce();
                }, interval, interval);

        mtrLogsTask = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> {
                    new MtrLogsScanner(plugin).scanOnce();
                }, interval / 2, interval);

        long worldDelay = Math.max(1L, interval / 3);
        mtrWorldTask = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> {
                    new MtrWorldScanner(plugin).scanOnce();
                }, worldDelay, interval);

        // periodically refresh UUID<->name mapping from playerdata
        nbtIdentityTask = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> {
                    new NbtIdentityScanner(plugin).scanOnce();
                }, interval, interval * 6); // less frequent after first run (~6x interval)
    }

    public void stop() {
        if (advancementsAndStatsTask != null) {
            advancementsAndStatsTask.cancel();
            advancementsAndStatsTask = null;
        }
        if (mtrLogsTask != null) {
            mtrLogsTask.cancel();
            mtrLogsTask = null;
        }
        if (nbtIdentityTask != null) {
            nbtIdentityTask.cancel();
            nbtIdentityTask = null;
        }
        if (mtrWorldTask != null) {
            mtrWorldTask.cancel();
            mtrWorldTask = null;
        }
    }
}
