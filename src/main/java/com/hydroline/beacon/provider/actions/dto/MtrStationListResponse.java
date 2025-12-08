package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MtrStationListResponse {
    private String dimension;
    private List<MtrDtos.StationInfo> stations = Collections.emptyList();

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public List<MtrDtos.StationInfo> getStations() {
        return stations;
    }

    public void setStations(List<MtrDtos.StationInfo> stations) {
        this.stations = stations == null ? Collections.emptyList() : stations;
    }
}
