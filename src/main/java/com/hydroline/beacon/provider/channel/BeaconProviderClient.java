package com.hydroline.beacon.provider.channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hydroline.beacon.BeaconPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * 负责与 Beacon Provider Mod 的 Plugin Messaging Channel 通信，提供按 action 的请求/响应接口。
 */
public final class BeaconProviderClient implements PluginMessageListener {
    public static final String CHANNEL_NAME = "hydroline:beacon_provider";
    public static final int PROTOCOL_VERSION = 1;

    private final BeaconPlugin plugin;
    private final ObjectMapper objectMapper;
    private final Map<String, PendingRequest<?>> pendingRequests = new ConcurrentHashMap<>();
    private volatile boolean started;

    public BeaconProviderClient(BeaconPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_NAME);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_NAME, this);
        started = true;
        plugin.getLogger().info("Beacon Provider channel registered: " + CHANNEL_NAME);
    }

    public synchronized void stop() {
        if (!started) {
            return;
        }
        started = false;
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL_NAME);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL_NAME, this);
        pendingRequests.forEach((requestId, pending) -> pending.completeExceptionally(
                new IllegalStateException("BeaconProviderClient stopped")));
        pendingRequests.clear();
        plugin.getLogger().info("Beacon Provider channel unregistered.");
    }

    public boolean isStarted() {
        return started;
    }

    public <T> CompletableFuture<BeaconActionResponse<T>> sendAction(BeaconActionCall<T> call) {
        if (!started) {
            throw new IllegalStateException("BeaconProviderClient has not been started yet");
        }
        Objects.requireNonNull(call, "call");

        String requestId = RequestIdGenerator.next();
        PendingRequest<T> pending = new PendingRequest<>(call);
        PendingRequest<?> previous = pendingRequests.put(requestId, pending);
        if (previous != null) {
            previous.completeExceptionally(new IllegalStateException("Duplicate requestId registered"));
        }

        ObjectNode payloadObject = preparePayload(call.getPayload());
        ObjectNode root = objectMapper.createObjectNode();
        root.put("protocolVersion", PROTOCOL_VERSION);
        root.put("requestId", requestId);
        root.put("action", call.getAction());
        root.set("payload", payloadObject);

        byte[] data;
        try {
            data = objectMapper.writeValueAsBytes(root);
        } catch (JsonProcessingException e) {
            pendingRequests.remove(requestId);
            throw new IllegalStateException("Failed to serialize beacon request", e);
        }

        scheduleTimeout(requestId, pending, call.getTimeout());
        dispatchMessage(data, requestId, pending);
        return pending.getFuture();
    }

    private void dispatchMessage(byte[] data, String requestId, PendingRequest<?> pending) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Bukkit.getServer().sendPluginMessage(plugin, CHANNEL_NAME, data);
            } catch (Exception ex) {
                pendingRequests.remove(requestId);
                pending.completeExceptionally(new IllegalStateException("Failed to dispatch plugin message", ex));
            }
        });
    }

    private void scheduleTimeout(String requestId, PendingRequest<?> pending, Duration timeout) {
        long timeoutTicks = Math.max(1L, (timeout.toMillis() + 49L) / 50L);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PendingRequest<?> removed = pendingRequests.remove(requestId);
            if (removed != null) {
                removed.completeExceptionally(new TimeoutException("Beacon Provider request timed out: " + requestId));
            }
        }, timeoutTicks);
        pending.setTimeoutTask(task);
    }

    private ObjectNode preparePayload(Object payload) {
        if (payload == null) {
            return objectMapper.createObjectNode();
        }
        if (payload instanceof ObjectNode) {
            return (ObjectNode) payload;
        }
        JsonNode node = objectMapper.valueToTree(payload);
        if (node != null && node.isObject()) {
            return (ObjectNode) node;
        }
        return objectMapper.createObjectNode();
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CHANNEL_NAME.equals(channel)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(message);
            String requestId = root.path("requestId").asText(null);
            if (requestId == null) {
                getLogger().warning("Received beacon response without requestId");
                return;
            }
            @SuppressWarnings("unchecked")
            PendingRequest<Object> pending = (PendingRequest<Object>) pendingRequests.remove(requestId);
            if (pending == null) {
                getLogger().warning("Received beacon response for unknown requestId=" + requestId);
                return;
            }

            int protocolVersion = root.path("protocolVersion").asInt(-1);
            String resultRaw = root.path("result").asText("ERROR");
            BeaconResultCode result = parseResult(resultRaw);
            String messageText = root.path("message").asText("");
            JsonNode payloadNode = root.path("payload");
            Object payload = null;
            if (payloadNode != null && !payloadNode.isMissingNode() && !payloadNode.isNull()) {
                payload = deserializePayload(payloadNode, pending.getCall());
            }

            BeaconActionResponse<Object> response = new BeaconActionResponse<>(
                    protocolVersion,
                    requestId,
                    result,
                    messageText,
                    payload
            );
            pending.complete(response);
        } catch (IOException ex) {
            getLogger().warning("Failed to parse beacon response: " + ex.getMessage());
        }
    }

    private Object deserializePayload(JsonNode payloadNode, BeaconActionCall<?> call) {
        Class<?> raw = call.getResponseType().getRawClass();
        if (raw == Void.class || raw == Void.TYPE) {
            return null;
        }
        return objectMapper.convertValue(payloadNode, call.getResponseType());
    }

    private BeaconResultCode parseResult(String raw) {
        try {
            return BeaconResultCode.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return BeaconResultCode.ERROR;
        }
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    private static final class PendingRequest<T> {
        private final BeaconActionCall<T> call;
        private final CompletableFuture<BeaconActionResponse<T>> future = new CompletableFuture<>();
        private BukkitTask timeoutTask;

        private PendingRequest(BeaconActionCall<T> call) {
            this.call = call;
        }

        public BeaconActionCall<T> getCall() {
            return call;
        }

        public CompletableFuture<BeaconActionResponse<T>> getFuture() {
            return future;
        }

        public void setTimeoutTask(BukkitTask timeoutTask) {
            this.timeoutTask = timeoutTask;
        }

        public void complete(BeaconActionResponse<T> response) {
            cancelTimeout();
            future.complete(response);
        }

        public void completeExceptionally(Throwable throwable) {
            cancelTimeout();
            future.completeExceptionally(throwable);
        }

        private void cancelTimeout() {
            if (timeoutTask != null) {
                timeoutTask.cancel();
                timeoutTask = null;
            }
        }
    }
}
