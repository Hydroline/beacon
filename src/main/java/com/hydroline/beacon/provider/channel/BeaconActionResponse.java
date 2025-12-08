package com.hydroline.beacon.provider.channel;

import java.util.Objects;

/**
 * 封装 Channel 响应 envelope，方便调用方获取 result、message 以及 payload。
 */
public final class BeaconActionResponse<T> {
    private final int protocolVersion;
    private final String requestId;
    private final BeaconResultCode result;
    private final String message;
    private final T payload;

    public BeaconActionResponse(int protocolVersion,
                                String requestId,
                                BeaconResultCode result,
                                String message,
                                T payload) {
        this.protocolVersion = protocolVersion;
        this.requestId = Objects.requireNonNull(requestId, "requestId");
        this.result = Objects.requireNonNull(result, "result");
        this.message = message == null ? "" : message;
        this.payload = payload;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getRequestId() {
        return requestId;
    }

    public BeaconResultCode getResult() {
        return result;
    }

    public boolean isOk() {
        return result == BeaconResultCode.OK;
    }

    public String getMessage() {
        return message;
    }

    public T getPayload() {
        return payload;
    }
}
