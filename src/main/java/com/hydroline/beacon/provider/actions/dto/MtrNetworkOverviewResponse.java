package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MtrNetworkOverviewResponse {
    private List<MtrDtos.DimensionOverview> dimensions = Collections.emptyList();

    public List<MtrDtos.DimensionOverview> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<MtrDtos.DimensionOverview> dimensions) {
        this.dimensions = dimensions == null ? Collections.emptyList() : dimensions;
    }
}
