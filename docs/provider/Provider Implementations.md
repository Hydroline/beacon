# Beacon Provider 需配合实现/扩展的 Action

本文根据当前 Bukkit 端接入情况（`SocketServerManager` 与 `BeaconProviderClient`）以及 Provider 仓库 `~/code/hydroline-beacon-provider` 中 `common/src/main/java/com/hydroline/beacon/provider/service/mtr` 的现状整理而成，供 Provider 侧排期实现。

## 1. 现有可直接使用的 Action

- `beacon:ping`（`PingActionHandler`）
- `mtr:list_network_overview`（`MtrListNetworkOverviewActionHandler`）
- `mtr:get_route_detail`（`MtrGetRouteDetailActionHandler`）
- `mtr:list_nodes_paginated`（`MtrListNodesPaginatedActionHandler`）
- `mtr:list_depots`（`MtrListDepotsActionHandler`）
- `mtr:list_fare_areas`（`MtrListFareAreasActionHandler`）
- `mtr:get_station_timetable`（`MtrGetStationTimetableActionHandler`）

Bukkit 端已对接上述 action，对应事件见 `docs/Socket IO API.md` 的“Beacon Provider 透传事件”章节。

## 2. 新增 action 状态（2025-12-10）

Provider 已交付以下 action，Bukkit/Web 端可直接消费：

### 2.1 `mtr:list_stations`

- 现已返回完整站点信息：`stationId/name/zone/bounds/interchangeRouteIds` 以及 `platforms[*] (platformId, platformName, routeIds, depotId)`。
- 数据来自 `RailwayData.stations` 与缓存映射，JSON 结构与 `MtrDtos.StationInfo` 对齐，可直接写入网站缓存。

### 2.2 `mtr:get_route_trains`

- 现在会输出真实列车列表，包含 `trainUuid`, `routeId`, `depotId`, `currentStationId`, `nextStationId`, `segmentCategory`, `progress`, `node`。
- 当前延误与节点坐标字段仍为 `null`，Provider 计划后续利用 `TrainServer` 路径与 `RailwayData.getTrainDelays()` 进一步补充。

### 2.3 `mtr:get_depot_trains`

- 返回指定车厂下所有列车的运行状态，结构与 `mtr:get_route_trains` 相同，并在顶层附带 `depotId`。
- `trainUuid` 使用维度 + MTR 列车 ID 生成的稳定 UUID，便于网站侧缓存与 diff。

## 3. 后续可选增强

1. **列车延误/坐标**：在 `MtrModels.TrainStatus` 中补齐 `delayMillis` 与节点 `x/y/z`（可从 `RailwayData.getTrainDelays()` / `TrainServer` 路径推算）。
2. **版本/时间戳**：可在 `mtr:list_stations` 等 action 中增加 `lastUpdated` 字段，帮助网站缓存做增量同步。
3. **限流策略**：若未来列车数量增多，可在 Provider 端加入简单缓存或速率限制，避免 Bukkit 频繁请求时阻塞主线程。

## 4. 调整记录

- 2025-12-09：Bukkit 端新增 `beacon_ping`, `get_mtr_network_overview`, `get_mtr_route_detail`, `list_mtr_nodes_paginated`, `list_mtr_depots`, `list_mtr_fare_areas`, `get_mtr_station_timetable`, `list_mtr_stations`, `get_mtr_route_trains`, `get_mtr_depot_trains` 等事件。
- 2025-12-10：Provider 完成 `mtr:list_stations`, `mtr:get_route_trains`, `mtr:get_depot_trains` 的实现并返回实时数据。
