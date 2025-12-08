package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MtrDepotTrainsResponse {
    private String dimension;
    private long depotId;
    private List<MtrDtos.TrainStatus> trains = Collections.emptyList();

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public long getDepotId() {
        return depotId;
    }

    public void setDepotId(long depotId) {
        this.depotId = depotId;
    }

    public List<MtrDtos.TrainStatus> getTrains() {
        return trains;
    }

    public void setTrains(List<MtrDtos.TrainStatus> trains) {
        this.trains = trains == null ? Collections.emptyList() : trains;
    }
}
