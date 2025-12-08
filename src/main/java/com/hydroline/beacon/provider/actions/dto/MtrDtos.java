package com.hydroline.beacon.provider.actions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/**
 * Bukkit 端使用的 MTR JSON DTO，字段与 Provider 输出保持对齐，便于后续写入数据库。
 */
public final class MtrDtos {
    private MtrDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DimensionOverview {
        private String dimension;
        private List<RouteSummary> routes = Collections.emptyList();
        private List<DepotInfo> depots = Collections.emptyList();
        private List<FareAreaInfo> fareAreas = Collections.emptyList();

        public String getDimension() {
            return dimension;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }

        public List<RouteSummary> getRoutes() {
            return routes;
        }

        public void setRoutes(List<RouteSummary> routes) {
            this.routes = routes == null ? Collections.emptyList() : routes;
        }

        public List<DepotInfo> getDepots() {
            return depots;
        }

        public void setDepots(List<DepotInfo> depots) {
            this.depots = depots == null ? Collections.emptyList() : depots;
        }

        public List<FareAreaInfo> getFareAreas() {
            return fareAreas;
        }

        public void setFareAreas(List<FareAreaInfo> fareAreas) {
            this.fareAreas = fareAreas == null ? Collections.emptyList() : fareAreas;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RouteSummary {
        private long routeId;
        private String name;
        private int color;
        private String transportMode;
        private String routeType;
        private boolean hidden;
        private List<PlatformSummary> platforms = Collections.emptyList();

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

        public String getTransportMode() {
            return transportMode;
        }

        public void setTransportMode(String transportMode) {
            this.transportMode = transportMode;
        }

        public String getRouteType() {
            return routeType;
        }

        public void setRouteType(String routeType) {
            this.routeType = routeType;
        }

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        public List<PlatformSummary> getPlatforms() {
            return platforms;
        }

        public void setPlatforms(List<PlatformSummary> platforms) {
            this.platforms = platforms == null ? Collections.emptyList() : platforms;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlatformSummary {
        private long platformId;
        private long stationId;
        private String stationName;
        private Bounds bounds;
        private List<Long> interchangeRouteIds = Collections.emptyList();

        public long getPlatformId() {
            return platformId;
        }

        public void setPlatformId(long platformId) {
            this.platformId = platformId;
        }

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

        public Bounds getBounds() {
            return bounds;
        }

        public void setBounds(Bounds bounds) {
            this.bounds = bounds;
        }

        public List<Long> getInterchangeRouteIds() {
            return interchangeRouteIds;
        }

        public void setInterchangeRouteIds(List<Long> interchangeRouteIds) {
            this.interchangeRouteIds = interchangeRouteIds == null
                    ? Collections.emptyList() : interchangeRouteIds;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bounds {
        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;

        public int getMinX() {
            return minX;
        }

        public void setMinX(int minX) {
            this.minX = minX;
        }

        public int getMinY() {
            return minY;
        }

        public void setMinY(int minY) {
            this.minY = minY;
        }

        public int getMinZ() {
            return minZ;
        }

        public void setMinZ(int minZ) {
            this.minZ = minZ;
        }

        public int getMaxX() {
            return maxX;
        }

        public void setMaxX(int maxX) {
            this.maxX = maxX;
        }

        public int getMaxY() {
            return maxY;
        }

        public void setMaxY(int maxY) {
            this.maxY = maxY;
        }

        public int getMaxZ() {
            return maxZ;
        }

        public void setMaxZ(int maxZ) {
            this.maxZ = maxZ;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DepotInfo {
        private long depotId;
        private String name;
        private String transportMode;
        private List<Long> routeIds = Collections.emptyList();
        private List<Integer> departures = Collections.emptyList();
        private boolean useRealTime;
        private boolean repeatInfinitely;
        private int cruisingAltitude;
        private Integer nextDepartureMillis;

        public long getDepotId() {
            return depotId;
        }

        public void setDepotId(long depotId) {
            this.depotId = depotId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTransportMode() {
            return transportMode;
        }

        public void setTransportMode(String transportMode) {
            this.transportMode = transportMode;
        }

        public List<Long> getRouteIds() {
            return routeIds;
        }

        public void setRouteIds(List<Long> routeIds) {
            this.routeIds = routeIds == null ? Collections.emptyList() : routeIds;
        }

        public List<Integer> getDepartures() {
            return departures;
        }

        public void setDepartures(List<Integer> departures) {
            this.departures = departures == null ? Collections.emptyList() : departures;
        }

        public boolean isUseRealTime() {
            return useRealTime;
        }

        public void setUseRealTime(boolean useRealTime) {
            this.useRealTime = useRealTime;
        }

        public boolean isRepeatInfinitely() {
            return repeatInfinitely;
        }

        public void setRepeatInfinitely(boolean repeatInfinitely) {
            this.repeatInfinitely = repeatInfinitely;
        }

        public int getCruisingAltitude() {
            return cruisingAltitude;
        }

        public void setCruisingAltitude(int cruisingAltitude) {
            this.cruisingAltitude = cruisingAltitude;
        }

        public Integer getNextDepartureMillis() {
            return nextDepartureMillis;
        }

        public void setNextDepartureMillis(Integer nextDepartureMillis) {
            this.nextDepartureMillis = nextDepartureMillis;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FareAreaInfo {
        private long stationId;
        private String name;
        private int zone;
        private Bounds bounds;
        private List<Long> interchangeRouteIds = Collections.emptyList();

        public long getStationId() {
            return stationId;
        }

        public void setStationId(long stationId) {
            this.stationId = stationId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getZone() {
            return zone;
        }

        public void setZone(int zone) {
            this.zone = zone;
        }

        public Bounds getBounds() {
            return bounds;
        }

        public void setBounds(Bounds bounds) {
            this.bounds = bounds;
        }

        public List<Long> getInterchangeRouteIds() {
            return interchangeRouteIds;
        }

        public void setInterchangeRouteIds(List<Long> interchangeRouteIds) {
            this.interchangeRouteIds = interchangeRouteIds == null
                    ? Collections.emptyList() : interchangeRouteIds;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RouteNode {
        private NodeInfo node;
        private String segmentCategory;
        private long sequence;

        public NodeInfo getNode() {
            return node;
        }

        public void setNode(NodeInfo node) {
            this.node = node;
        }

        public String getSegmentCategory() {
            return segmentCategory;
        }

        public void setSegmentCategory(String segmentCategory) {
            this.segmentCategory = segmentCategory;
        }

        public long getSequence() {
            return sequence;
        }

        public void setSequence(long sequence) {
            this.sequence = sequence;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeInfo {
        private int x;
        private int y;
        private int z;
        private String railType;
        private boolean platformSegment;
        private Long stationId;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public String getRailType() {
            return railType;
        }

        public void setRailType(String railType) {
            this.railType = railType;
        }

        public boolean isPlatformSegment() {
            return platformSegment;
        }

        public void setPlatformSegment(boolean platformSegment) {
            this.platformSegment = platformSegment;
        }

        public Long getStationId() {
            return stationId;
        }

        public void setStationId(Long stationId) {
            this.stationId = stationId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StationInfo {
        private long stationId;
        private String name;
        private int zone;
        private Bounds bounds;
        private List<Long> interchangeRouteIds = Collections.emptyList();
        private List<StationPlatformInfo> platforms = Collections.emptyList();

        public long getStationId() {
            return stationId;
        }

        public void setStationId(long stationId) {
            this.stationId = stationId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getZone() {
            return zone;
        }

        public void setZone(int zone) {
            this.zone = zone;
        }

        public Bounds getBounds() {
            return bounds;
        }

        public void setBounds(Bounds bounds) {
            this.bounds = bounds;
        }

        public List<Long> getInterchangeRouteIds() {
            return interchangeRouteIds;
        }

        public void setInterchangeRouteIds(List<Long> interchangeRouteIds) {
            this.interchangeRouteIds = interchangeRouteIds == null
                    ? Collections.emptyList() : interchangeRouteIds;
        }

        public List<StationPlatformInfo> getPlatforms() {
            return platforms;
        }

        public void setPlatforms(List<StationPlatformInfo> platforms) {
            this.platforms = platforms == null ? Collections.emptyList() : platforms;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StationPlatformInfo {
        private long platformId;
        private String platformName;
        private List<Long> routeIds = Collections.emptyList();
        private Long depotId;

        public long getPlatformId() {
            return platformId;
        }

        public void setPlatformId(long platformId) {
            this.platformId = platformId;
        }

        public String getPlatformName() {
            return platformName;
        }

        public void setPlatformName(String platformName) {
            this.platformName = platformName;
        }

        public List<Long> getRouteIds() {
            return routeIds;
        }

        public void setRouteIds(List<Long> routeIds) {
            this.routeIds = routeIds == null ? Collections.emptyList() : routeIds;
        }

        public Long getDepotId() {
            return depotId;
        }

        public void setDepotId(Long depotId) {
            this.depotId = depotId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrainStatus {
        private String trainUuid;
        private long routeId;
        private Long depotId;
        private String transportMode;
        private Long currentStationId;
        private Long nextStationId;
        private Integer delayMillis;
        private String segmentCategory;
        private Double progress;
        private NodeInfo node;

        public String getTrainUuid() {
            return trainUuid;
        }

        public void setTrainUuid(String trainUuid) {
            this.trainUuid = trainUuid;
        }

        public long getRouteId() {
            return routeId;
        }

        public void setRouteId(long routeId) {
            this.routeId = routeId;
        }

        public Long getDepotId() {
            return depotId;
        }

        public void setDepotId(Long depotId) {
            this.depotId = depotId;
        }

        public String getTransportMode() {
            return transportMode;
        }

        public void setTransportMode(String transportMode) {
            this.transportMode = transportMode;
        }

        public Long getCurrentStationId() {
            return currentStationId;
        }

        public void setCurrentStationId(Long currentStationId) {
            this.currentStationId = currentStationId;
        }

        public Long getNextStationId() {
            return nextStationId;
        }

        public void setNextStationId(Long nextStationId) {
            this.nextStationId = nextStationId;
        }

        public Integer getDelayMillis() {
            return delayMillis;
        }

        public void setDelayMillis(Integer delayMillis) {
            this.delayMillis = delayMillis;
        }

        public String getSegmentCategory() {
            return segmentCategory;
        }

        public void setSegmentCategory(String segmentCategory) {
            this.segmentCategory = segmentCategory;
        }

        public Double getProgress() {
            return progress;
        }

        public void setProgress(Double progress) {
            this.progress = progress;
        }

        public NodeInfo getNode() {
            return node;
        }

        public void setNode(NodeInfo node) {
            this.node = node;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlatformTimetable {
        private long platformId;
        private List<ScheduleEntry> entries = Collections.emptyList();

        public long getPlatformId() {
            return platformId;
        }

        public void setPlatformId(long platformId) {
            this.platformId = platformId;
        }

        public List<ScheduleEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<ScheduleEntry> entries) {
            this.entries = entries == null ? Collections.emptyList() : entries;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScheduleEntry {
        private long routeId;
        private long arrivalMillis;
        private int trainCars;
        private int currentStationIndex;
        private Integer delayMillis;

        public long getRouteId() {
            return routeId;
        }

        public void setRouteId(long routeId) {
            this.routeId = routeId;
        }

        public long getArrivalMillis() {
            return arrivalMillis;
        }

        public void setArrivalMillis(long arrivalMillis) {
            this.arrivalMillis = arrivalMillis;
        }

        public int getTrainCars() {
            return trainCars;
        }

        public void setTrainCars(int trainCars) {
            this.trainCars = trainCars;
        }

        public int getCurrentStationIndex() {
            return currentStationIndex;
        }

        public void setCurrentStationIndex(int currentStationIndex) {
            this.currentStationIndex = currentStationIndex;
        }

        public Integer getDelayMillis() {
            return delayMillis;
        }

        public void setDelayMillis(Integer delayMillis) {
            this.delayMillis = delayMillis;
        }
    }
}
