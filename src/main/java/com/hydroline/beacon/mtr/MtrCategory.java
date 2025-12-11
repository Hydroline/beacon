package com.hydroline.beacon.mtr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public enum MtrCategory {
    DEPOTS("depots", "mtr_depots", "id",
            Arrays.asList("entity_id", "name", "color", "last_updated"),
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("entity_id", "transport_mode", "name", "color", "file_path"))),
            true, true, true),
    PLATFORMS("platforms", "mtr_platforms", "id",
            Arrays.asList("entity_id", "name", "color", "last_updated"),
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("entity_id", "transport_mode", "name", "color", "file_path"))),
            true, true, true),
    RAILS("rails", "mtr_rails", "node_pos",
            Arrays.asList("entity_id", "last_updated"),
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("entity_id", "file_path"))),
            false, false, false),
    ROUTES("routes", "mtr_routes", "id",
            Arrays.asList("entity_id", "name", "color", "last_updated"),
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("entity_id", "transport_mode", "name", "color", "file_path"))),
            true, true, true),
    SIGNAL_BLOCKS("signal-blocks", "mtr_signal_blocks", "id",
            Arrays.asList("entity_id", "color", "last_updated"),
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("entity_id", "transport_mode", "color", "file_path"))),
            true, false, true),
    STATIONS("stations", "mtr_stations", "id",
            Arrays.asList("entity_id", "name", "color", "last_updated"),
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("entity_id", "transport_mode", "name", "color", "file_path"))),
            true, true, true);

    private final String key;
    private final String tableName;
    private final String idField;
    private final List<String> orderableColumns;
    private final Set<String> filterableColumns;
    private final boolean hasTransportModeColumn;
    private final boolean hasNameColumn;
    private final boolean hasColorColumn;
    private final List<String> metadataColumns;

    MtrCategory(String key,
                String tableName,
                String idField,
                List<String> orderableColumns,
                Set<String> filterableColumns,
                boolean hasTransportModeColumn,
                boolean hasNameColumn,
                boolean hasColorColumn) {
        this.key = key;
        this.tableName = tableName;
        this.idField = idField;
        this.orderableColumns = Collections.unmodifiableList(new ArrayList<>(orderableColumns));
        this.filterableColumns = Collections.unmodifiableSet(new HashSet<>(filterableColumns));
        this.hasTransportModeColumn = hasTransportModeColumn;
        this.hasNameColumn = hasNameColumn;
        this.hasColorColumn = hasColorColumn;
        List<String> metadata = new ArrayList<>();
        if (hasTransportModeColumn) {
            metadata.add("transport_mode");
        }
        if (hasNameColumn) {
            metadata.add("name");
        }
        if (hasColorColumn) {
            metadata.add("color");
        }
        this.metadataColumns = Collections.unmodifiableList(metadata);
    }

    public String getKey() {
        return key;
    }

    public String getTableName() {
        return tableName;
    }

    public String getIdField() {
        return idField;
    }

    public List<String> getOrderableColumns() {
        return orderableColumns;
    }

    public boolean isFilterableColumn(String column) {
        return filterableColumns.contains(column);
    }

    public List<String> getMetadataColumns() {
        return metadataColumns;
    }

    public boolean hasTransportModeColumn() {
        return hasTransportModeColumn;
    }

    public boolean hasNameColumn() {
        return hasNameColumn;
    }

    public boolean hasColorColumn() {
        return hasColorColumn;
    }

    public static MtrCategory fromKey(String key) {
        if (key == null) {
            return null;
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        for (MtrCategory category : values()) {
            if (category.key.equalsIgnoreCase(normalized)) {
                return category;
            }
        }
        return null;
    }
}
