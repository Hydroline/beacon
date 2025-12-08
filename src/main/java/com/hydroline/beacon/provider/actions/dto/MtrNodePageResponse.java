package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MtrNodePageResponse {
    private String dimension;
    private List<MtrDtos.NodeInfo> nodes = Collections.emptyList();
    private String nextCursor;
    private boolean hasMore;

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public List<MtrDtos.NodeInfo> getNodes() {
        return nodes;
    }

    public void setNodes(List<MtrDtos.NodeInfo> nodes) {
        this.nodes = nodes == null ? Collections.emptyList() : nodes;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
