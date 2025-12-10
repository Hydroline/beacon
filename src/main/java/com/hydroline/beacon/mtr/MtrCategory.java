package com.hydroline.beacon.mtr;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public enum MtrCategory {
    DEPOTS("depots", "mtr_depots", "id"),
    PLATFORMS("platforms", "mtr_platforms", "id"),
    RAILS("rails", "mtr_rails", "node_pos"),
    ROUTES("routes", "mtr_routes", "id"),
    SIGNAL_BLOCKS("signal-blocks", "mtr_signal_blocks", "id"),
    STATIONS("stations", "mtr_stations", "id");

    private static final List<String> ORDERABLE_COLUMNS =
            Collections.unmodifiableList(Arrays.asList("entity_id", "name", "color", "last_updated"));
    private static final Set<String> FILTERABLE_COLUMNS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("entity_id", "transport_mode", "name", "color", "file_path")
    ));

    private final String key;
    private final String tableName;
    private final String idField;

    MtrCategory(String key, String tableName, String idField) {
        this.key = key;
        this.tableName = tableName;
        this.idField = idField;
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
        return ORDERABLE_COLUMNS;
    }

    public boolean isFilterableColumn(String column) {
        return FILTERABLE_COLUMNS.contains(column);
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
