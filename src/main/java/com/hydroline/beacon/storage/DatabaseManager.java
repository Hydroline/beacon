package com.hydroline.beacon.storage;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final Plugin plugin;
    private final String jdbcUrl;
    private static final int CURRENT_SCHEMA_VERSION = 2;
    private static final String CREATE_MTR_RAILS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS mtr_rails (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "dimension_context TEXT NOT NULL," +
                    "entity_id TEXT NOT NULL," +
                    "file_path TEXT NOT NULL," +
                    "payload TEXT NOT NULL," +
                    "last_updated INTEGER NOT NULL," +
                    "UNIQUE(dimension_context, entity_id)" +
            ")";
    private static final String CREATE_MTR_SIGNAL_BLOCKS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS mtr_signal_blocks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "dimension_context TEXT NOT NULL," +
                    "entity_id TEXT NOT NULL," +
                    "transport_mode TEXT," +
                    "color INTEGER," +
                    "file_path TEXT NOT NULL," +
                    "payload TEXT NOT NULL," +
                    "last_updated INTEGER NOT NULL," +
                    "UNIQUE(dimension_context, entity_id)" +
            ")";

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, "hydroline_beacon.db");
        // Add busy_timeout to reduce SQLITE_BUSY under concurrent writers
        this.jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath() + "?busy_timeout=5000";
    }

    public void initialize() throws SQLException {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("PRAGMA journal_mode=WAL");
                statement.executeUpdate("PRAGMA busy_timeout=5000");
            }
            createSchema(connection);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    private void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS schema_version (" +
                            "id INTEGER PRIMARY KEY CHECK (id = 1)," +
                            "version INTEGER NOT NULL," +
                            "updated_at INTEGER NOT NULL" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_sessions (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "event_type TEXT NOT NULL," +
                            "occurred_at INTEGER NOT NULL," +
                            "player_uuid TEXT NOT NULL," +
                            "player_name TEXT," +
                            "player_ip TEXT," +
                            "world_name TEXT," +
                            "dimension_key TEXT," +
                            "x REAL," +
                            "y REAL," +
                            "z REAL" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_advancements (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "player_uuid TEXT NOT NULL," +
                            "advancement_key TEXT NOT NULL," +
                            "value BLOB NOT NULL," +
                            "last_updated INTEGER NOT NULL," +
                            "UNIQUE(player_uuid, advancement_key)" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_stats (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "player_uuid TEXT NOT NULL," +
                            "stat_key TEXT NOT NULL," +
                            "value INTEGER NOT NULL," +
                            "last_updated INTEGER NOT NULL," +
                            "UNIQUE(player_uuid, stat_key)" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mtr_logs (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "timestamp TEXT," +
                            "player_name TEXT," +
                            "player_uuid TEXT," +
                            "class_name TEXT," +
                            "entry_id TEXT," +
                            "entry_name TEXT," +
                            "position TEXT," +
                            "change_type TEXT," +
                            "old_data TEXT," +
                            "new_data TEXT," +
                            "source_file_path TEXT," +
                            "source_line INTEGER," +
                            "dimension_context TEXT" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mtr_files (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "file_path TEXT NOT NULL UNIQUE," +
                            "last_modified INTEGER NOT NULL," +
                            "last_processed INTEGER," +
                            "processed INTEGER NOT NULL DEFAULT 0," +
                            "dimension_context TEXT" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS file_sync_state (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "file_type TEXT NOT NULL," +
                            "player_uuid TEXT," +
                            "file_path TEXT NOT NULL," +
                            "last_modified INTEGER NOT NULL," +
                            "last_processed INTEGER," +
                            "UNIQUE(file_type, file_path)" +
                            ")"
            );

            // Map UUID <-> last known player name (from playerdata NBT or other sources)
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_identities (" +
                            "player_uuid TEXT PRIMARY KEY," +
                            "player_name TEXT," +
                            "first_played INTEGER," +
                            "last_played INTEGER," +
                            "last_updated INTEGER NOT NULL" +
                            ")"
            );

            // backfill columns for existing deployments (SQLite throws if column exists)
            try {
                statement.executeUpdate("ALTER TABLE player_identities ADD COLUMN first_played INTEGER");
            } catch (SQLException ignored) {}
            try {
                statement.executeUpdate("ALTER TABLE player_identities ADD COLUMN last_played INTEGER");
            } catch (SQLException ignored) {}

            // Cache of raw player NBT JSON to avoid heavy parsing on every request
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_nbt_cache (" +
                            "player_uuid TEXT PRIMARY KEY," +
                            "raw_json TEXT NOT NULL," +
                            "cached_at INTEGER NOT NULL" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mtr_world_files (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "file_path TEXT NOT NULL UNIQUE," +
                            "category TEXT NOT NULL," +
                            "dimension_context TEXT NOT NULL," +
                            "namespace TEXT," +
                            "dimension TEXT," +
                            "last_modified INTEGER NOT NULL," +
                            "last_processed INTEGER," +
                            "last_seen_at INTEGER NOT NULL," +
                            "processed INTEGER NOT NULL DEFAULT 0" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mtr_depots (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "dimension_context TEXT NOT NULL," +
                            "entity_id TEXT NOT NULL," +
                            "transport_mode TEXT," +
                            "name TEXT," +
                            "color INTEGER," +
                            "file_path TEXT NOT NULL," +
                            "payload TEXT NOT NULL," +
                            "last_updated INTEGER NOT NULL," +
                            "UNIQUE(dimension_context, entity_id)" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mtr_platforms (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "dimension_context TEXT NOT NULL," +
                            "entity_id TEXT NOT NULL," +
                            "transport_mode TEXT," +
                            "name TEXT," +
                            "color INTEGER," +
                            "file_path TEXT NOT NULL," +
                            "payload TEXT NOT NULL," +
                            "last_updated INTEGER NOT NULL," +
                            "UNIQUE(dimension_context, entity_id)" +
                            ")"
            );

            statement.executeUpdate(CREATE_MTR_RAILS_TABLE_SQL);

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mtr_routes (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "dimension_context TEXT NOT NULL," +
                            "entity_id TEXT NOT NULL," +
                            "transport_mode TEXT," +
                            "name TEXT," +
                            "color INTEGER," +
                            "file_path TEXT NOT NULL," +
                            "payload TEXT NOT NULL," +
                            "last_updated INTEGER NOT NULL," +
                            "UNIQUE(dimension_context, entity_id)" +
                            ")"
            );

            statement.executeUpdate(CREATE_MTR_SIGNAL_BLOCKS_TABLE_SQL);

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mtr_stations (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "dimension_context TEXT NOT NULL," +
                            "entity_id TEXT NOT NULL," +
                            "transport_mode TEXT," +
                            "name TEXT," +
                            "color INTEGER," +
                            "file_path TEXT NOT NULL," +
                            "payload TEXT NOT NULL," +
                            "last_updated INTEGER NOT NULL," +
                            "UNIQUE(dimension_context, entity_id)" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mtr_entity_diffs (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "category TEXT NOT NULL," +
                            "dimension_context TEXT NOT NULL," +
                            "entity_id TEXT NOT NULL," +
                            "change_type TEXT NOT NULL CHECK (change_type IN ('added','updated','deleted'))," +
                            "before_payload TEXT," +
                            "after_payload TEXT," +
                            "file_path TEXT," +
                            "processed_at INTEGER NOT NULL" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mtr_dimension_versions (" +
                            "dimension_context TEXT PRIMARY KEY," +
                            "namespace TEXT," +
                            "dimension TEXT," +
                            "last_updated INTEGER NOT NULL" +
                            ")"
            );
        }

        applyMigrations(connection);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mtr_world_files_category ON mtr_world_files(category)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mtr_world_files_dimension ON mtr_world_files(dimension_context)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mtr_depots_dimension ON mtr_depots(dimension_context)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mtr_platforms_dimension ON mtr_platforms(dimension_context)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mtr_rails_dimension ON mtr_rails(dimension_context)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mtr_routes_dimension ON mtr_routes(dimension_context)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mtr_signal_blocks_dimension ON mtr_signal_blocks(dimension_context)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_mtr_stations_dimension ON mtr_stations(dimension_context)");
        }
    }

    private void applyMigrations(Connection connection) throws SQLException {
        if (shouldMigrateMtrRails(connection)) {
            migrateMtrRails(connection);
        }
        if (shouldMigrateMtrSignalBlocks(connection)) {
            migrateMtrSignalBlocks(connection);
        }
        upsertSchemaVersion(connection, CURRENT_SCHEMA_VERSION);
    }

    private boolean shouldMigrateMtrRails(Connection connection) throws SQLException {
        return tableHasColumn(connection, "mtr_rails", "transport_mode") ||
                tableHasColumn(connection, "mtr_rails", "name") ||
                tableHasColumn(connection, "mtr_rails", "color");
    }

    private boolean shouldMigrateMtrSignalBlocks(Connection connection) throws SQLException {
        return tableHasColumn(connection, "mtr_signal_blocks", "name");
    }

    private void migrateMtrRails(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP INDEX IF EXISTS idx_mtr_rails_dimension");
            statement.executeUpdate("ALTER TABLE mtr_rails RENAME TO mtr_rails_old");
            statement.executeUpdate(CREATE_MTR_RAILS_TABLE_SQL);
            statement.executeUpdate(
                    "INSERT INTO mtr_rails (dimension_context, entity_id, file_path, payload, last_updated) " +
                            "SELECT dimension_context, entity_id, file_path, payload, last_updated FROM mtr_rails_old"
            );
            statement.executeUpdate("DROP TABLE mtr_rails_old");
        }
    }

    private void migrateMtrSignalBlocks(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP INDEX IF EXISTS idx_mtr_signal_blocks_dimension");
            statement.executeUpdate("ALTER TABLE mtr_signal_blocks RENAME TO mtr_signal_blocks_old");
            statement.executeUpdate(CREATE_MTR_SIGNAL_BLOCKS_TABLE_SQL);
            statement.executeUpdate(
                    "INSERT INTO mtr_signal_blocks (dimension_context, entity_id, transport_mode, color, file_path, payload, last_updated) " +
                            "SELECT dimension_context, entity_id, transport_mode, color, file_path, payload, last_updated FROM mtr_signal_blocks_old"
            );
            statement.executeUpdate("DROP TABLE mtr_signal_blocks_old");
        }
    }

    private boolean tableHasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String existing = resultSet.getString("name");
                if (existing != null && existing.equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void upsertSchemaVersion(Connection connection, int version) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO schema_version (id, version, updated_at) VALUES (1, ?, ?) " +
                        "ON CONFLICT(id) DO UPDATE SET version = excluded.version, updated_at = excluded.updated_at"
        )) {
            long now = System.currentTimeMillis();
            statement.setInt(1, version);
            statement.setLong(2, now);
            statement.executeUpdate();
        }
    }
}
