package com.hydroline.beacon.provider.channel;

import java.security.SecureRandom;

/**
 * 生成与 Beacon Provider Mod 一致的 12 位 requestId，字符集 [0-9a-z]。
 */
public final class RequestIdGenerator {
    public static final int LENGTH = 12;
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private RequestIdGenerator() {
    }

    public static String next() {
        char[] buffer = new char[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            buffer[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(buffer);
    }
}
