package com.hydroline.beacon.util;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class PathUtils {

    private PathUtils() {}

    /**
     * Returns the server root directory (the parent directory that contains the world folders).
     */
    public static File getServerRoot(Plugin plugin) {
        File worldContainer = plugin.getServer().getWorldContainer();
        if (worldContainer != null) {
            return worldContainer.getAbsoluteFile();
        }
        // Fallback: plugins/<name> -> plugins -> <server-root>
        File data = plugin.getDataFolder();
        File pluginsDir = data != null ? data.getParentFile() : null;
        File root = pluginsDir != null ? pluginsDir.getParentFile() : null;
        return (root != null ? root : new File(".")).getAbsoluteFile();
    }

    /**
     * Convert a file path to a canonical relative path from the server root.
     * Always uses forward slashes and removes any dot-segments.
     */
    public static String toServerRelativePath(Plugin plugin, File file) {
        File base = getServerRoot(plugin);
        try {
            File canonBase = base.getCanonicalFile();
            File canonFile = file.getCanonicalFile();
            Path basePath = canonBase.toPath();
            Path filePath = canonFile.toPath();
            Path rel = basePath.relativize(filePath);
            return rel.toString().replace(File.separatorChar, '/');
        } catch (IOException | IllegalArgumentException e) {
            // Fallback: normalized absolute path with forward slashes
            return file.getAbsolutePath().replace(File.separatorChar, '/');
        }
    }
}
