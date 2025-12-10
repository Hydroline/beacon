package com.hydroline.beacon.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydroline.beacon.BeaconPlugin;
import com.hydroline.beacon.config.PluginConfig;
import com.hydroline.beacon.mtr.MtrCategory;
import com.hydroline.beacon.storage.DatabaseManager;
import com.hydroline.beacon.util.MtrMessagePackDecoder;
import com.hydroline.beacon.util.PathUtils;
import com.hydroline.beacon.world.WorldFileAccess;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MtrWorldScanner {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final List<String> CATEGORIES = Collections.unmodifiableList(
            Arrays.asList("depots", "platforms", "rails", "routes", "signal-blocks", "stations")
    );
    private static final long BATCH_PAUSE_MS = 500L;

    private final BeaconPlugin plugin;

    public MtrWorldScanner(BeaconPlugin plugin) {
        this.plugin = plugin;
    }

    public void scanOnce() {
        PluginConfig cfg = plugin.getConfigManager().getCurrentConfig();
        if (cfg == null || !cfg.isMtrWorldScanEnabled()) {
            return;
        }
        WorldFileAccess worldFileAccess = plugin.getWorldFileAccess();
        DatabaseManager db = plugin.getDatabaseManager();
        if (worldFileAccess == null || db == null) {
            return;
        }

        long scanStart = System.currentTimeMillis();
        File serverRoot = PathUtils.getServerRoot(plugin);
        if (serverRoot == null) {
            plugin.getLogger().warning("Unable to determine server root for MTR scan");
            return;
        }

        int batchSize = cfg.getMtrWorldScanBatchSize();
        if (batchSize <= 0) {
            batchSize = 16;
        }

        int filesProcessed = 0;
        int entityChanges = 0;

        try (Connection connection = db.getConnection()) {
            connection.setAutoCommit(false);
            for (World world : worldFileAccess.getWorlds()) {
                collectWorldFiles(world, connection, scanStart);
            }
            handleDeletedFiles(connection, scanStart);
            boolean interrupted = false;
            while (true) {
                List<MtrWorldFile> pending = fetchPendingFiles(connection, batchSize);
                if (pending.isEmpty()) {
                    break;
                }
                for (MtrWorldFile file : pending) {
                    try {
                        int delta = processFile(connection, file, serverRoot, scanStart);
                        if (delta >= 0) {
                            filesProcessed++;
                            entityChanges += delta;
                        }
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to read MTR file " + file.filePath + ": " + e.getMessage());
                    } catch (SQLException e) {
                        plugin.getLogger().warning("DB error while processing MTR file " + file.filePath + ": " + e.getMessage());
                    }
                }
                if (pending.size() < batchSize) {
                    break;
                }
                if (sleepBetweenBatches()) {
                    interrupted = true;
                    break;
                }
            }
            connection.commit();
            if (interrupted) {
                plugin.getLogger().warning("MTR world scan interrupted before finishing all batches");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to scan MTR world data: " + e.getMessage());
            return;
        }

        if (filesProcessed > 0) {
            plugin.getLogger().info("MTR world scan processed " + filesProcessed + " files, entity changes=" + entityChanges);
        }
    }

    private void collectWorldFiles(World world, Connection connection, long scanStart) throws SQLException {
        File worldFolder = world.getWorldFolder();
        File mtrRoot = new File(worldFolder, "mtr");
        if (!mtrRoot.isDirectory()) {
            return;
        }
        for (File namespaceDir : mtrRoot.listFiles(File::isDirectory)) {
            if (namespaceDir == null || !namespaceDir.isDirectory()) {
                continue;
            }
            String namespace = namespaceDir.getName();
            for (File dimensionDir : namespaceDir.listFiles(File::isDirectory)) {
                if (dimensionDir == null || !dimensionDir.isDirectory()) {
                    continue;
                }
                String dimension = dimensionDir.getName();
                String dimensionContext = String.join("/", "mtr", namespace, dimension);
                for (String category : CATEGORIES) {
                    File categoryDir = new File(dimensionDir, category);
                    if (!categoryDir.isDirectory()) {
                        continue;
                    }
                    visitCategoryFiles(categoryDir, category, namespace, dimension, dimensionContext, connection, scanStart);
                }
            }
        }
    }

    private void visitCategoryFiles(File root,
                                    String category,
                                    String namespace,
                                    String dimension,
                                    String dimensionContext,
                                    Connection connection,
                                    long scanStart) throws SQLException {
        Deque<File> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            File current = stack.pop();
            File[] children = current.listFiles();
            if (children == null) {
                continue;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    stack.push(child);
                    continue;
                }
                if (!child.isFile()) {
                    continue;
                }
                String relativePath = PathUtils.toServerRelativePath(plugin, child);
                touchWorldFileRecord(connection, relativePath, category, namespace, dimension, dimensionContext,
                        child.lastModified(), scanStart);
            }
        }
    }

    private void touchWorldFileRecord(Connection connection,
                                      String relativePath,
                                      String category,
                                      String namespace,
                                      String dimension,
                                      String dimensionContext,
                                      long lastModified,
                                      long scanStart) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT last_modified FROM mtr_world_files WHERE file_path = ?"
        )) {
            select.setString(1, relativePath);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    long stored = rs.getLong("last_modified");
                    if (stored != lastModified) {
                        try (PreparedStatement update = connection.prepareStatement(
                                "UPDATE mtr_world_files SET category = ?, dimension_context = ?, namespace = ?, dimension = ?, last_modified = ?, last_seen_at = ?, processed = 0 WHERE file_path = ?"
                        )) {
                            update.setString(1, category);
                            update.setString(2, dimensionContext);
                            update.setString(3, namespace);
                            update.setString(4, dimension);
                            update.setLong(5, lastModified);
                            update.setLong(6, scanStart);
                            update.setString(7, relativePath);
                            update.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement updateSeen = connection.prepareStatement(
                                "UPDATE mtr_world_files SET last_seen_at = ? WHERE file_path = ?"
                        )) {
                            updateSeen.setLong(1, scanStart);
                            updateSeen.setString(2, relativePath);
                            updateSeen.executeUpdate();
                        }
                    }
                    return;
                }
            }
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO mtr_world_files (file_path, category, dimension_context, namespace, dimension, last_modified, last_seen_at, processed) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, 0)"
        )) {
            insert.setString(1, relativePath);
            insert.setString(2, category);
            insert.setString(3, dimensionContext);
            insert.setString(4, namespace);
            insert.setString(5, dimension);
            insert.setLong(6, lastModified);
            insert.setLong(7, scanStart);
            insert.executeUpdate();
        }
    }

    private List<MtrWorldFile> fetchPendingFiles(Connection connection, int limit) throws SQLException {
        List<MtrWorldFile> files = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT file_path, category, dimension_context, namespace, dimension, last_modified " +
                        "FROM mtr_world_files WHERE processed = 0 ORDER BY last_seen_at DESC LIMIT ?"
        )) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MtrWorldFile file = new MtrWorldFile();
                    file.filePath = rs.getString("file_path");
                    file.category = rs.getString("category");
                    file.dimensionContext = rs.getString("dimension_context");
                    file.namespace = rs.getString("namespace");
                    file.dimension = rs.getString("dimension");
                    file.lastModified = rs.getLong("last_modified");
                    files.add(file);
                }
            }
        }
        return files;
    }

    private void handleDeletedFiles(Connection connection, long scanStart) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, file_path, category, dimension_context FROM mtr_world_files WHERE last_seen_at < ?"
        )) {
            ps.setLong(1, scanStart);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String categoryKey = rs.getString("category");
                    String dimensionContext = rs.getString("dimension_context");
                    String filePath = rs.getString("file_path");
                    MtrCategory category = MtrCategory.fromKey(categoryKey);
                    if (category != null) {
                        deleteEntityForMissingFile(connection, category, dimensionContext, filePath);
                    }
                    try (PreparedStatement delete = connection.prepareStatement(
                            "DELETE FROM mtr_world_files WHERE id = ?"
                    )) {
                        delete.setLong(1, id);
                        delete.executeUpdate();
                    }
                }
            }
        }
    }

    private void deleteEntityForMissingFile(Connection connection,
                                            MtrCategory category,
                                            String dimensionContext,
                                            String filePath) throws SQLException {
        String table = category.getTableName();
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT entity_id, payload FROM " + table + " WHERE file_path = ? AND dimension_context = ? LIMIT 1"
        )) {
            select.setString(1, filePath);
            select.setString(2, dimensionContext);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    String entityId = rs.getString("entity_id");
                    String payload = rs.getString("payload");
                    insertDiff(connection, category.getKey(), dimensionContext, entityId, "deleted", payload, null, filePath, System.currentTimeMillis());
                }
            }
        }
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM " + table + " WHERE file_path = ? AND dimension_context = ?"
        )) {
            delete.setString(1, filePath);
            delete.setString(2, dimensionContext);
            delete.executeUpdate();
        }
    }

    private int processFile(Connection connection, MtrWorldFile file, File serverRoot, long scanStart) throws SQLException, IOException {
        File actual = new File(serverRoot, file.filePath);
        if (!actual.isFile()) {
            return -1;
        }
        List<Map<String, Object>> records = decodeRecords(Files.readAllBytes(actual.toPath()));
        int changed = 0;
        for (Map<String, Object> record : records) {
            if (storeEntity(connection, file, record, scanStart)) {
                changed++;
            }
        }
        markFileProcessed(connection, file.filePath, scanStart, true);
        updateDimensionVersion(connection, file.dimensionContext, file.namespace, file.dimension, scanStart);
        return changed;
    }

    private List<Map<String, Object>> decodeRecords(byte[] raw) {
        Object decoded = MtrMessagePackDecoder.decode(raw);
        if (decoded instanceof List) {
            List<?> list = (List<?>) decoded;
            List<Map<String, Object>> records = new ArrayList<>(list.size());
            for (Object item : list) {
                Map<String, Object> map = normalizeMap(item);
                if (map != null) {
                    records.add(map);
                }
            }
            return records;
        }
        Map<String, Object> map = normalizeMap(decoded);
        return map != null ? Collections.singletonList(map) : Collections.emptyList();
    }

    private Map<String, Object> normalizeMap(Object value) {
        if (!(value instanceof Map)) {
            return null;
        }
        Map<?, ?> raw = (Map<?, ?>) value;
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            normalized.put(entry.getKey().toString(), entry.getValue());
        }
        return normalized;
    }

    private boolean storeEntity(Connection connection, MtrWorldFile file, Map<String, Object> record, long scannedAt) throws SQLException {
        MtrCategory category = MtrCategory.fromKey(file.category);
        if (category == null) {
            return false;
        }
        String entityId = getEntityId(record, category);
        if (entityId == null || entityId.isEmpty()) {
            return false;
        }
        String payload = toJson(record);
        String table = category.getTableName();
        String transportMode = toString(record.get("transport_mode"));
        String name = toString(record.get("name"));
        Long color = toLong(record.get("color"));

        String existingPayload = null;
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT payload FROM " + table + " WHERE dimension_context = ? AND entity_id = ?"
        )) {
            select.setString(1, file.dimensionContext);
            select.setString(2, entityId);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    existingPayload = rs.getString("payload");
                }
            }
        }

        if (existingPayload == null) {
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO " + table + " (dimension_context, entity_id, transport_mode, name, color, file_path, payload, last_updated) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            )) {
                insert.setString(1, file.dimensionContext);
                insert.setString(2, entityId);
                insert.setString(3, transportMode);
                insert.setString(4, name);
                if (color != null) {
                    insert.setLong(5, color);
                } else {
                    insert.setNull(5, java.sql.Types.INTEGER);
                }
                insert.setString(6, file.filePath);
                insert.setString(7, payload);
                insert.setLong(8, scannedAt);
                insert.executeUpdate();
            }
            insertDiff(connection, category.getKey(), file.dimensionContext, entityId, "added", null, payload, file.filePath, scannedAt);
            return true;
        }

        if (!existingPayload.equals(payload)) {
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE " + table + " SET transport_mode = ?, name = ?, color = ?, file_path = ?, payload = ?, last_updated = ? " +
                            "WHERE dimension_context = ? AND entity_id = ?"
            )) {
                update.setString(1, transportMode);
                update.setString(2, name);
                if (color != null) {
                    update.setLong(3, color);
                } else {
                    update.setNull(3, java.sql.Types.INTEGER);
                }
                update.setString(4, file.filePath);
                update.setString(5, payload);
                update.setLong(6, scannedAt);
                update.setString(7, file.dimensionContext);
                update.setString(8, entityId);
                update.executeUpdate();
            }
            insertDiff(connection, category.getKey(), file.dimensionContext, entityId, "updated", existingPayload, payload, file.filePath, scannedAt);
            return true;
        }
        return false;
    }

    private void markFileProcessed(Connection connection, String filePath, long scanStart, boolean success) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE mtr_world_files SET last_processed = ?, processed = ? WHERE file_path = ?"
        )) {
            update.setLong(1, scanStart);
            update.setInt(2, success ? 1 : 0);
            update.setString(3, filePath);
            update.executeUpdate();
        }
    }

    private void insertDiff(Connection connection,
                            String category,
                            String dimensionContext,
                            String entityId,
                            String changeType,
                            String beforePayload,
                            String afterPayload,
                            String filePath,
                            long processedAt) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO mtr_entity_diffs (category, dimension_context, entity_id, change_type, before_payload, after_payload, file_path, processed_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        )) {
            ps.setString(1, category);
            ps.setString(2, dimensionContext);
            ps.setString(3, entityId);
            ps.setString(4, changeType);
            ps.setString(5, beforePayload);
            ps.setString(6, afterPayload);
            ps.setString(7, filePath);
            ps.setLong(8, processedAt);
            ps.executeUpdate();
        }
    }

    private void updateDimensionVersion(Connection connection,
                                        String dimensionContext,
                                        String namespace,
                                        String dimension,
                                        long timestamp) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO mtr_dimension_versions (dimension_context, namespace, dimension, last_updated) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT(dimension_context) DO UPDATE SET namespace = excluded.namespace, dimension = excluded.dimension, last_updated = excluded.last_updated"
        )) {
            ps.setString(1, dimensionContext);
            ps.setString(2, namespace);
            ps.setString(3, dimension);
            ps.setLong(4, timestamp);
            ps.executeUpdate();
        }
    }

    private String getEntityId(Map<String, Object> record, MtrCategory category) {
        Object value = record.get(category.getIdField());
        return value != null ? value.toString() : null;
    }

    private String toJson(Map<String, Object> record) {
        try {
            return JSON.writeValueAsString(JSON.valueToTree(record));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize MTR record", e);
        }
    }

    private String toString(Object value) {
        return value != null ? value.toString() : null;
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean sleepBetweenBatches() {
        try {
            Thread.sleep(BATCH_PAUSE_MS);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }

    private static final class MtrWorldFile {
        private String filePath;
        private String category;
        private String dimensionContext;
        private String namespace;
        private String dimension;
        private long lastModified;
    }
}
