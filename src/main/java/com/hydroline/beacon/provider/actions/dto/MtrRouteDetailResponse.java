package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MtrRouteDetailResponse {
    private String dimension;
    private long routeId;
    private String name;
    private int color;
    private String routeType;
    private List<MtrDtos.RouteNode> nodes = Collections.emptyList();

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public List<MtrDtos.RouteNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<MtrDtos.RouteNode> nodes) {
        this.nodes = nodes == null ? Collections.emptyList() : nodes;
    }
}
