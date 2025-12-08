package com.hydroline.beacon.provider.channel;

/**
 * 与 Beacon Provider Mod 共享的状态码，映射 Channel API 文档中的 result 字段。
 */
public enum BeaconResultCode {
    OK,
    BUSY,
    INVALID_ACTION,
    INVALID_PAYLOAD,
    NOT_READY,
    ERROR
}
