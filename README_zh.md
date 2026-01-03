# beacon

> [英文](README.md)。本项目与 Beacon Provider 紧密配合。

Beacon 插件是面向 Bukkit/Spigot 的遥测工具，通过 Socket.IO 将 Minecraft 服务器数据暴露给后台或其他消费者。它会收集玩家、世界事件，以及 MTR/Create 模组日志，统一存入 SQLite 以供查询。

Beacon 插件必须和 Beacon Provider 联动：Provider 提供 modpack 级的遥测数据，Beacon 插件则启动 Socket.IO 通道并把数据同步给外部系统。

插件通过一系列 Socket.IO 事件提供实时和历史数据，方便远程监控与分析服务器状态。

概括来说，Beacon 插件负责维持 SQLite 数据库并暴露远程查询能力，内部驱动负责锁与协调逻辑。

## 目录结构

```
src/main/
├── java/com/hydroline/beacon/
│   ├── BeaconPlugin.java          # 插件入口
│   ├── config/                    # 配置管理
│   ├── gateway/                   # Socket.IO 网关与处理
│   ├── listener/                  # 事件监听器
│   ├── mtr/                       # MTR 相关逻辑
│   ├── provider/                  # 数据提供方支持
│   ├── socket/                    # Socket.IO 服务器控制
│   ├── storage/                   # 数据库操作
│   ├── task/                      # 定时任务
│   ├── util/                      # 工具类
│   └── world/                     # 世界文件访问
└── resources/
    ├── config.yml                 # 默认配置
    └── plugin.yml                 # 插件元信息
```

## 配置

插件会在 `plugins/Hydroline-Beacon/config.yml`（源文件：`src/main/resources/config.yml`）生成默认配置。

| 字段                        | 含义                                           | 默认    |
| --------------------------- | ---------------------------------------------- | ------- |
| `port`                      | Socket.IO 服务监听端口                         | `48080` |
| `key`                       | `/beacon` 命令的 64 字节密钥（务必改为随机值） | `""`    |
| `interval_time`             | 扫描间隔（tick，1 秒=20 tick，默认 200）       | `200`   |
| `mtr_world_scan_enabled`    | 是否扫描 MTR 世界结构数据                      | `true`  |
| `mtr_world_scan_batch_size` | 每次世界扫描的文件批次数                       | `16`    |
| `nbt_cache_ttl_minutes`     | `get_player_nbt` JSON 缓存有效期（分钟）       | `10`    |
| `default_language`          | 当无法匹配执行者 locale 时的默认命令语言       | `zh_cn` |
| `version`                   | 配置版本（与插件保持一致即可）                 | `1`     |

配置变更后重启插件即可生效。插件也会自动读取 `./config` 下的 Beacon Provider 设置文件。

## 命令使用

`/beacon` 命令默认输出简体中文；如果执行者的 Locale（例如控制台通常是 `en_us`）匹配了其他 `lang/messages_{locale}.properties`，会优先加载对应翻译，找不到则回退到英文。

> 所有命令都需要 `hydroline.beacon.admin` 权限。

| 命令                         | 说明                                                                             |
| ---------------------------- | -------------------------------------------------------------------------------- |
| `/beacon` 或 `/beacon help`  | 显示命令帮助                                                                     |
| `/beacon list`               | 列出当前所有 Socket.IO 客户端，含连接 ID、IP、传输方式、User-Agent 等            |
| `/beacon provider status`    | 显示 Provider 网关是否启用/已连接、心跳间隔、挂起请求数、重连延迟、Provider 版本 |
| `/beacon sync nbt`           | 手动刷新 `player_nbt_cache`（PlayerData NBT 缓存）                               |
| `/beacon sync scans`         | 立即执行进度/统计、MTR 日志及世界扫描器                                          |
| `/beacon query <SELECT ...>` | 在 SQLite 中执行只读单条 SELECT（禁止分号），最多返回 5 行                       |
| `/beacon stats`              | 输出主要数据表行数及最近更新时间                                                 |
| `/beacon info`               | 展示当前配置（端口/扫描间隔/版本/NBT TTL）及 Provider 网关状态                   |

## 构建

```bash
# 构建项目
./gradlew build

# 输出 JAR 位于 build/libs/
```

## 集成测试

Socket.IO API 集成测试位于 `tests/`：

```bash
cd tests
pnpm install
node test-socketio.js
```

## 部署

1. 执行 `./gradlew build`
2. 将 `build/libs/` 里的 JAR 拷贝到目标服务器 `plugins/`
3. 重启服务器或执行 `/reload`
4. 配置 `plugins/Hydroline-Beacon/config.yml`
5. 暴露前设定安全的 API Key

## 事件

插件会通过 Socket.IO 暴露多个事件，详情请参考 [docs/Socket IO API.md](docs/Socket%20IO%20API.md)
