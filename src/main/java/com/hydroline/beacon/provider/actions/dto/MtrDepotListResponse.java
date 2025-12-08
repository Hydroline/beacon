package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MtrDepotListResponse {
    private List<MtrDtos.DepotInfo> depots = Collections.emptyList();

    public List<MtrDtos.DepotInfo> getDepots() {
        return depots;
    }

    public void setDepots(List<MtrDtos.DepotInfo> depots) {
        this.depots = depots == null ? Collections.emptyList() : depots;
    }
}
