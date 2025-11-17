import { io } from "socket.io-client";

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
      await emitWithAck(socket, "get_server_time", { key }, "get_server_time");
      await emitWithAck(socket, "list_online_players", { key }, "list_online_players");

      if (playerUuid) {
        await emitWithAck(
          socket,
          "get_player_advancements",
          { key, playerUuid },
          "get_player_advancements"
        );
        await emitWithAck(
          socket,
          "get_player_stats",
          { key, playerUuid },
          "get_player_stats"
        );
      } else {
        console.log("BEACON_PLAYER_UUID not set, skip player-specific queries.");
      }

      await emitWithAck(socket, "force_update", { key }, "force_update");
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

function emitWithAck(socket, event, payload, label) {
  return new Promise((resolve, reject) => {
    console.log(`\n>>> Emitting ${event} with payload:`, payload);
    socket.timeout(10000).emit(event, payload, (err, response) => {
      if (err) {
        console.error(`<<< [${label}] ACK error:`, err);
        reject(err);
        return;
      }
      console.log(`<<< [${label}] ACK response:`, response);
      resolve(response);
    });
  });
}

main().catch((err) => {
  console.error("Fatal error:", err);
  process.exit(1);
});

