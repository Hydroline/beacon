package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MtrFareAreaListResponse {
    private String dimension;
    private List<MtrDtos.FareAreaInfo> fareAreas = Collections.emptyList();

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public List<MtrDtos.FareAreaInfo> getFareAreas() {
        return fareAreas;
    }

    public void setFareAreas(List<MtrDtos.FareAreaInfo> fareAreas) {
        this.fareAreas = fareAreas == null ? Collections.emptyList() : fareAreas;
    }
}
