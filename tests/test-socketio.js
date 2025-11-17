import io from "socket.io-client";
import fs from "node:fs";
import path from "node:path";
import dotenv from "dotenv";

dotenv.config();

function requireEnv(name) {
  const value = process.env[name];
  if (!value) {
    throw new Error(`Missing required environment variable: ${name}`);
  }
  return value;
}

async function main() {
  const host = process.env.BEACON_HOST || "127.0.0.1";
  const port = requireEnv("BEACON_PORT");
  const key = requireEnv("BEACON_KEY");
  const playerUuid = process.env.BEACON_PLAYER_UUID;
  const outDir = process.env.OUTPUT_DIR || path.resolve(process.cwd(), "output");
  ensureDir(outDir);

  const url = `http://${host}:${port}`;
  console.log(`Connecting to ${url} ...`);

  const socket = io(url, {
    transports: ["websocket"],
    reconnectionAttempts: 3,
    timeout: 10000
  });

  socket.on("connect", async () => {
    console.log("Connected, socket id =", socket.id);

    try {
      const serverTime = await emitWithAck(socket, "get_server_time", { key }, "get_server_time");
      await writeJson(outDir, "server_time.json", serverTime);

      const onlinePlayers = await emitWithAck(socket, "list_online_players", { key }, "list_online_players");
      await writeJson(outDir, "online_players.json", onlinePlayers);

      if (playerUuid) {
        const adv = await emitWithAck(
          socket,
          "get_player_advancements",
          { key, playerUuid },
          "get_player_advancements"
        );
        await writeJson(outDir, `advancements_${sanitize(playerUuid)}.json`, adv);

        const stats = await emitWithAck(
          socket,
          "get_player_stats",
          { key, playerUuid },
          "get_player_stats"
        );
        await writeJson(outDir, `stats_${sanitize(playerUuid)}.json`, stats);
      } else {
        console.log("BEACON_PLAYER_UUID not set, skip player-specific queries.");
      }

      const force = await emitWithAck(socket, "force_update", { key }, "force_update");
      await writeJson(outDir, `force_update.json`, force);
    } catch (err) {
      console.error("Test error:", err.message);
    } finally {
      socket.close();
    }
  });

  socket.on("connect_error", (err) => {
    console.error("Connect error:", err.message);
  });

  socket.on("error", (err) => {
    console.error("Socket error:", err);
  });
}

function ensureDir(dir) {
  try {
    fs.mkdirSync(dir, { recursive: true });
  } catch (e) {
    // ignore
  }
}

function sanitize(name) {
  return (name || "").replace(/[^a-zA-Z0-9_.-]/g, "_");
}

async function writeJson(outDir, fileName, data) {
  const target = path.join(outDir, fileName);
  const payload = {
    timestamp: new Date().toISOString(),
    data: data === undefined ? null : data
  };
  await fs.promises.writeFile(target, JSON.stringify(payload, null, 2) + "\n", "utf8");
  console.log(`Wrote ${target}`);
}

function emitWithAck(socket, event, payload, label) {
  return new Promise((resolve, reject) => {
    console.log(`\n>>> Emitting ${event} with payload:`, payload);
    const timer = setTimeout(() => {
      reject(new Error(`${label} ack timeout`));
    }, 10000);

    // socket.io v2 ack callback receives only the response args (no error-first)
    socket.emit(event, payload, (...args) => {
      clearTimeout(timer);
      if (args.length === 0) {
        console.log(`<<< [${label}] ACK response: <no-args>`);
        resolve(undefined);
        return;
      }
      console.log(`<<< [${label}] ACK response args:`, args);
      resolve(args[0]);
    });
  });
}

main().catch((err) => {
  console.error("Fatal error:", err);
  process.exit(1);
});

