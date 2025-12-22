package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MtrAllStationSchedulesResponse {
    private long timestamp;
    private String dimension;
    private String note;
    private List<DimensionSchedules> dimensions = Collections.emptyList();

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<DimensionSchedules> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<DimensionSchedules> dimensions) {
        this.dimensions = dimensions == null ? Collections.emptyList() : dimensions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DimensionSchedules {
        private String dimension;
        private List<StationSchedules> stations = Collections.emptyList();

        public String getDimension() {
            return dimension;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }

        public List<StationSchedules> getStations() {
            return stations;
        }

        public void setStations(List<StationSchedules> stations) {
            this.stations = stations == null ? Collections.emptyList() : stations;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StationSchedules {
        private long stationId;
        private String stationName;
        private List<MtrDtos.PlatformTimetable> platforms = Collections.emptyList();

        public long getStationId() {
            return stationId;
        }

        public void setStationId(long stationId) {
            this.stationId = stationId;
        }

        public String getStationName() {
            return stationName;
        }

        public void setStationName(String stationName) {
            this.stationName = stationName;
        }

        public List<MtrDtos.PlatformTimetable> getPlatforms() {
            return platforms;
        }

        public void setPlatforms(List<MtrDtos.PlatformTimetable> platforms) {
            this.platforms = platforms == null ? Collections.emptyList() : platforms;
        }
    }
}
