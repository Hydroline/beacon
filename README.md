# beacon

> [中文](README_zh.md)。This project is tightly coupled with the Beacon Provider.

Beacon Plugin is a Bukkit/Spigot telemetry plugin that exposes Minecraft server information via Socket.IO to backend services or other consumers. It collects and aggregates data from players, world events, and MTR, Create mod logs, storing them in SQLite for querying.

Beacon Plugin is designed to work in conjunction with Beacon Provider. The Provider supplies modpack-specific telemetry data while Beacon Plugin is responsible for initializing Socket.IO communication to expose server information to the backend or other consumers.

The plugin offers real-time and historical data through Socket.IO events, allowing remote monitoring and data analysis of Minecraft servers.

In short, the Beacon Plugin is primarily responsible for maintaining the SQLite database
and providing remote query capabilities. The drivers act as a management layer,
handling locking and coordination internally.

## Layout

```
src/main/
├── java/com/hydroline/beacon/
│   ├── BeaconPlugin.java          # Main plugin entrypoint
│   ├── config/                    # Configuration handling
│   ├── gateway/                   # Socket.IO gateway and handlers
│   ├── listener/                  # Event listeners
│   ├── mtr/                       # MTR-specific logic
│   ├── provider/                  # Data providers
│   ├── socket/                    # Socket.IO server management
│   ├── storage/                   # Database operations
│   ├── task/                      # Scheduled tasks
│   ├── util/                      # Utility functions
│   └── world/                     # World data handling
└── resources/
    ├── config.yml                 # Default configuration
    └── plugin.yml                 # Plugin metadata
```

## Configuration

The plugin configuration is located at `plugins/Hydroline-Beacon/config.yml` (source: `src/main/resources/config.yml`).

Key configuration options:

- `port`: Socket.IO server port
- `key`: 64-byte secret key for authentication (set a secure random value)
- `interval_time`: Scan interval in ticks (1 second = 20 ticks, default: 200 = 10 seconds)
- `mtr_world_scan_enabled`: Enable scanning of MTR world data structures (default: true)
- `mtr_world_scan_batch_size`: Maximum files per scan batch (default: 16)

Also, the plugin automatically scans the Beacon Provider configuration files in `./config`.

## Configuration example (defaults)

When you first run the plugin, it will create `plugins/Hydroline-Beacon/config.yml` populated with defaults. Below is the default setting shipped with `src/main/resources/config.yml`:

| Field                       | Description                                               | Default |
| --------------------------- | --------------------------------------------------------- | ------- |
| `port`                      | Socket.IO server port                                     | `48080` |
| `key`                       | 64-byte secret for `/beacon` authentication               | `""`    |
| `interval_time`             | Task scan interval in ticks (1 tick = 0.05s)              | `200`   |
| `mtr_world_scan_enabled`    | Enable scanning of MTR world structures                   | `true`  |
| `mtr_world_scan_batch_size` | Files per batch during world scan                         | `16`    |
| `nbt_cache_ttl_minutes`     | TTL (minutes) for cached `get_player_nbt` JSON            | `10`    |
| `default_language`          | Default `/beacon` command language when no locale matched | `zh_cn` |
| `version`                   | Configuration version number (kept in sync with plugin)   | `1`     |

## Command usage

Beacon commands default to simplified Chinese (zh_cn). When a CommandSender runs `/beacon`, the plugin reads the player's or console locale, loads the most specific translation resource that exists, and falls back to `lang/messages_en_us.properties` if no match is found. All commands require `hydroline.beacon.admin`.

| Command                      | Description                                                                                                                                       |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| `/beacon list`               | Lists connected Socket.IO clients with details such as connection ID, IP/port, transport, and user agent.                                         |
| `/beacon provider status`    | Shows whether the Beacon Provider gateway is enabled, connected, heartbeat interval, pending requests, reconnect delay, and provider mod version. |
| `/beacon sync nbt`           | Forces a manual refresh of the `player_nbt_cache` table by scanning playerdata files.                                                             |
| `/beacon sync scans`         | Manually runs the advancement/stat scan, MTR log scan, and MTR world scan to keep data fresh.                                                     |
| `/beacon query <SELECT ...>` | Executes a read-only SQL query (single `SELECT`, no semicolons) against the SQLite database, returning up to five rows.                           |
| `/beacon stats`              | Reports counts for key tables plus the most recent recorded timestamps.                                                                           |
| `/beacon info`               | Displays current configuration (port, scan interval, plugin version, NBT TTL) and whether the Provider gateway is enabled.                        |
| `/beacon help`               | Prints the command usage synopsis (same as `/beacon`).                                                                                            |

## Building

```bash
# Build the project
./gradlew build

# The compiled JAR will be in build/libs/
```

Socket.IO API integration tests are available in the `tests/` directory:

```bash
cd tests
pnpm install
node test-socketio.js
```

## Deployment

1. Build the plugin: `./gradlew build`
2. Copy the JAR from `build/libs/` to your Bukkit server's `plugins/` directory
3. Restart the server or use `/reload` command
4. Configure `plugins/Hydroline-Beacon/config.yml` with your server settings
5. Set a secure API key before exposing to the network

## Events

The plugin exposes the following Socket.IO events. See [Socket IO API.md](docs/Socket%20IO%20API.md) for detailed documentation.
