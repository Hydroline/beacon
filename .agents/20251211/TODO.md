- [x] 完成 MTR 结构化扫描总体规划 —— 调研 `~/sandbox/mohist-1.18.2/world/mtr/minecraft/overworld` 目录（包含 depots/platforms/rails/routes/signal-blocks/stations）及 Provider `tools/mtr-world-dump.js` MessagePack 解码方案，明确后续实施范围与约束。

- [x] 定义 SQLite Schema —— 为 `mtr_depots/mtr_platforms/mtr_rails/mtr_routes/mtr_signal_blocks/mtr_stations` 及对应 `mtr_*_diffs`/`mtr_data_version` 表设计字段、索引、主键策略（如 `dimension_context + file_id + entity_id`），并规划 JSON/几何字段存储格式，确保与现有 `mtr_logs`、`file_sync_state` 协调。

- [x] 建立 MessagePack 解码与文件枚举层 —— 复刻/提炼 `mtr-world-dump.js` 解码逻辑为 JVM 版本（或嵌入 JS runtime）并支持多世界（namespace/dimension）递归遍历；同时实现 `FileManifest` 抽象，缓存 `last_modified/size/hash` 以便 diff。

- [x] 构建 `MtrDataScanner` 异步批处理框架 —— 在 `ScanScheduler` 下游注册独立任务，按维度 + 类别拆分 job，限制单 tick 处理数量（如文件队列 + worker pool），支持暂停/恢复/回压，并将扫描进度记录到新的 `mtr_scan_queue`/`mtr_scan_state` 表以实现断点续扫。

- [x] 实现 Diff 逻辑与历史追踪 —— 每次解析 MessagePack 后与上次镜像比对：仅对新增/变更实体写入 `mtr_*` 主表，另写 diff 表（含 `entity_id`, `change_type`, `before_json`, `after_json`, `file_path`, `processed_at`）；提供 TTL/压缩策略，保证大图线路更新时不会阻塞。

- [x] 扩展 API 层（GraphQL + Socket.IO） —— 设计新的 `get_mtr_*` 事件/GraphQL schema：支持 Depot/Route/Station/Signal 等单表或多表 JOIN 查询，允许根据 `dimension_context`、`version`, `updated_since` 过滤；复用 `execute_sql` 安全层或新增 DAO，并确保响应分页/排序。

- [x] 运行期配置与可观测性 —— 在 `plugin-config.json`/`PluginConfig` 引入开关（启用目录、扫描频率、最大并发、diff 保存期），添加日志/metrics（扫描耗时、文件/实体计数、错误统计），必要时提供 `socket event` 触发即时全量扫描以辅助运维。
