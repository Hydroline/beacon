package com.hydroline.beacon.provider.channel;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.time.Duration;
import java.util.Objects;

/**
 * 表示一次 action 调用：action 名称、payload 以及预期的 payload 类型。
 */
public final class BeaconActionCall<T> {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    private final String action;
    private final Object payload;
    private final JavaType responseType;
    private final Duration timeout;

    private BeaconActionCall(String action, Object payload, JavaType responseType, Duration timeout) {
        this.action = Objects.requireNonNull(action, "action");
        this.payload = payload;
        this.responseType = Objects.requireNonNull(responseType, "responseType");
        this.timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
    }

    public static <T> BeaconActionCall<T> of(String action, Object payload, Class<T> responseClass) {
        return new BeaconActionCall<>(action, payload, TYPE_FACTORY.constructType(responseClass), DEFAULT_TIMEOUT);
    }

    public static <T> BeaconActionCall<T> of(String action, Object payload, JavaType responseType) {
        return new BeaconActionCall<>(action, payload, responseType, DEFAULT_TIMEOUT);
    }

    public BeaconActionCall<T> withTimeout(Duration timeout) {
        return new BeaconActionCall<>(action, payload, responseType, timeout);
    }

    public String getAction() {
        return action;
    }

    public Object getPayload() {
        return payload;
    }

    public JavaType getResponseType() {
        return responseType;
    }

    public Duration getTimeout() {
        return timeout;
    }
}
