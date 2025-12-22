package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MtrStationScheduleResponse {
    private long timestamp;
    private String dimension;
    private long stationId;
    private List<Timetable> timetables = Collections.emptyList();

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

    public long getStationId() {
        return stationId;
    }

    public void setStationId(long stationId) {
        this.stationId = stationId;
    }

    public List<Timetable> getTimetables() {
        return timetables;
    }

    public void setTimetables(List<Timetable> timetables) {
        this.timetables = timetables == null ? Collections.emptyList() : timetables;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Timetable {
        private String dimension;
        private List<MtrDtos.PlatformTimetable> platforms = Collections.emptyList();

        public String getDimension() {
            return dimension;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }

        public List<MtrDtos.PlatformTimetable> getPlatforms() {
            return platforms;
        }

        public void setPlatforms(List<MtrDtos.PlatformTimetable> platforms) {
            this.platforms = platforms == null ? Collections.emptyList() : platforms;
        }
    }
}
