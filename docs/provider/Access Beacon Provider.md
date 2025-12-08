# Beacon Provider Channel 接入说明

本文记录 Bukkit 插件内如何对接 Beacon Provider Mod，包含 Channel 注册、请求/响应结构以及预置 action builder 的使用方式，方便后续把 MTR/Fabric 侧的数据写入 SQLite。

## 1. Channel 初始化

- Channel 名称：`hydroline:beacon_provider`（详见 `docs/provider/docs/Channel API.md`）。
- 插件启动时 `BeaconProviderClient` 会自动调用 `Bukkit.getMessenger()` 注册双向 Channel 并监听响应；插件关闭时自动反注册并取消所有未完成请求。
- `BeaconProviderClient` 位于 `com.hydroline.beacon.provider.channel`，通过 `BeaconPlugin#getBeaconProviderClient()` 取得实例后即可发起请求。

```java
BeaconProviderClient client = plugin.getBeaconProviderClient();
if (client != null && client.isStarted()) {
    client.sendAction(BeaconProviderActions.ping("web"))
        .thenAccept(response -> {
            if (response.isOk()) {
                BeaconPingResponse payload = response.getPayload();
                plugin.getLogger().info("Beacon Provider 延迟=" + payload.getLatencyMs() + "ms");
            } else {
                plugin.getLogger().warning("Beacon Provider ping 失败: " + response.getMessage());
            }
        });
}
```

> `sendAction` 返回 `CompletableFuture<BeaconActionResponse<T>>`，内部已根据 `requestId` 映射响应；默认超时 10s，可通过 `BeaconActionCall#withTimeout(Duration)` 自定义。

## 2. Action Builder

`com.hydroline.beacon.provider.actions.BeaconProviderActions` 提供了当前 Provider 文档列出的 action 便捷构造器：

| 方法 | 对应 action | 说明 |
| ---- | ----------- | ---- |
| `ping(echo)` | `beacon:ping` | 通道连通性/延迟探测，`payload` 可选 `echo` 字段。 |
| `listNetworkOverview(dimension)` | `mtr:list_network_overview` | 可选过滤维度；响应包含每个维度的线路、车厂、收费区概览。 |
| `getRouteDetail(dimension, routeId)` | `mtr:get_route_detail` | 维度 + 路线 ID，返回节点序列、颜色、类型等。 |
| `listDepots(dimension)` | `mtr:list_depots` | 可选维度过滤，输出 `departures`、`routeIds` 等信息。 |
| `listFareAreas(dimension)` | `mtr:list_fare_areas` | 需要指定维度，返回站点/收费区多边形。 |
| `listNodesPaginated(dimension, cursor, limit)` | `mtr:list_nodes_paginated` | 维度必填，支持 `cursor` + `limit` 分页同步节点。 |
| `getStationTimetable(dimension, stationId, platformId)` | `mtr:get_station_timetable` | 维度 + 站点 ID，`platformId` 可选，响应含 `entries`（到站、延误）。 |

调用示例（分页拉取节点）：

```java
client.sendAction(BeaconProviderActions.listNodesPaginated("minecraft:overworld", null, 512))
        .thenAccept(response -> {
            if (!response.isOk()) {
                plugin.getLogger().warning("list_nodes_paginated 失败: " + response.getMessage());
                return;
            }
            MtrNodePageResponse page = response.getPayload();
            page.getNodes().forEach(node -> {
                // TODO: 写入 SQLite
            });
            if (page.isHasMore()) {
                // 递归请求下一页
            }
        });
```

## 3. DTO 与扩展

- 所有响应 DTO 位于 `com.hydroline.beacon.provider.actions.dto`，字段结构与 Provider `MtrJsonWriter` 输出一致并标记 `@JsonIgnoreProperties(ignoreUnknown = true)`，Protocol 扩展时无需立刻改动 Bukkit 端。
- 若 Provider 新增 action，可：
  1. 在 Bukkit 端自定义 `ObjectNode` payload；
  2. 通过 `BeaconActionCall.of("action:name", payload, ResponseClass.class)` 直接发送；
  3. 按需在 `actions/dto` 包新增响应 DTO，或直接将 payload 映射到 `JsonNode`/`Map`。
- `BeaconProviderClient` 暴露的 `sendAction` 支持任意 `BeaconActionCall`，因此新增 action 只需在 Bukkit 项目内追加 builder 与 DTO 即可。

## 4. Socket.IO 事件映射

- `SocketServerManager` 已注册以下事件并直接调用 `BeaconProviderClient`：
  - `beacon_ping`
  - `get_mtr_network_overview`
  - `get_mtr_route_detail`
  - `list_mtr_nodes_paginated`
  - `list_mtr_depots`
  - `list_mtr_fare_areas`
  - `get_mtr_station_timetable`
  - `list_mtr_stations`
  - `get_mtr_route_trains`
  - `get_mtr_depot_trains`
- 事件响应统一包含 `success/result/message/request_id/payload` 字段；详见 `docs/Socket IO API.md` 中的“Beacon Provider 透传事件”章节。

## 5. 下一步

- 当前仅完成 Channel 与 DTO 接入，待后续任务把响应写入 SQLite 并透出给 Socket.IO/HTTP 端点。
- 如需长时间任务，可在 `sendAction` 后使用 `CompletableFuture#orTimeout`/`completeOnTimeout` 做更细粒度控制。
