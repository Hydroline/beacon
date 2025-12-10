package com.hydroline.beacon.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MtrMessagePackDecoder {

    private final byte[] buffer;
    private int offset;

    private MtrMessagePackDecoder(byte[] buffer) {
        this.buffer = buffer;
        this.offset = 0;
    }

    public static Object decode(byte[] buffer) {
        try {
            MtrMessagePackDecoder decoder = new MtrMessagePackDecoder(buffer);
            return decoder.readValue();
        } catch (IndexOutOfBoundsException e) {
            throw new MtrMessagePackException("Unexpected end of MessagePack buffer");
        }
    }

    private Object readValue() {
        ensureAvailable(1);
        int prefix = readByte() & 0xFF;
        if (prefix <= 0x7F) {
            return prefix;
        }
        if (prefix >= 0x80 && prefix <= 0x8F) {
            return readMap(prefix & 0x0F);
        }
        if (prefix >= 0x90 && prefix <= 0x9F) {
            return readArray(prefix & 0x0F);
        }
        if (prefix >= 0xA0 && prefix <= 0xBF) {
            return readString(prefix & 0x1F);
        }
        switch (prefix) {
            case 0xC0:
                return null;
            case 0xC2:
                return false;
            case 0xC3:
                return true;
            case 0xC4:
                return readBinary(readUInt(1));
            case 0xC5:
                return readBinary(readUInt(2));
            case 0xC6:
                return readBinary(readUInt(4));
            case 0xCA:
                return readFloat32();
            case 0xCB:
                return readFloat64();
            case 0xCC:
                return readUInt(1);
            case 0xCD:
                return readUInt(2);
            case 0xCE:
                return readUInt(4);
            case 0xCF:
                return readUInt64();
            case 0xD0:
                return readInt(1);
            case 0xD1:
                return readInt(2);
            case 0xD2:
                return readInt(4);
            case 0xD3:
                return readInt64();
            case 0xD9:
                return readString(readUInt(1));
            case 0xDA:
                return readString(readUInt(2));
            case 0xDB:
                return readString(readUInt(4));
            case 0xDC:
                return readArray(readUInt(2));
            case 0xDD:
                return readArray(readUInt(4));
            case 0xDE:
                return readMap(readUInt(2));
            case 0xDF:
                return readMap(readUInt(4));
            default:
                if (prefix >= 0xE0 && prefix <= 0xFF) {
                    return (long) (byte) prefix;
                }
        }
        throw new MtrMessagePackException("Unsupported MessagePack prefix: 0x" + Integer.toHexString(prefix));
    }

    private Object readArray(long length) {
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new MtrMessagePackException("Array length out of range: " + length);
        }
        int size = (int) length;
        List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readValue());
        }
        return list;
    }

    private Object readMap(long length) {
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new MtrMessagePackException("Map length out of range: " + length);
        }
        int size = (int) length;
        Map<String, Object> map = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            Object key = readValue();
            Object value = readValue();
            String keyStr = key != null ? key.toString() : null;
            map.put(keyStr, value);
        }
        return map;
    }

    private String readString(long length) {
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new MtrMessagePackException("String length out of range: " + length);
        }
        ensureAvailable((int) length);
        int len = (int) length;
        String value = new String(buffer, offset, len, StandardCharsets.UTF_8);
        offset += len;
        return value;
    }

    private String readBinary(long length) {
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new MtrMessagePackException("Binary length out of range: " + length);
        }
        ensureAvailable((int) length);
        byte[] slice = new byte[(int) length];
        System.arraycopy(buffer, offset, slice, 0, slice.length);
        offset += slice.length;
        return Base64.getEncoder().encodeToString(slice);
    }

    private Number readFloat32() {
        ensureAvailable(4);
        int bits = (readUnsignedByte() << 24) |
                (readUnsignedByte() << 16) |
                (readUnsignedByte() << 8) |
                readUnsignedByte();
        return Float.intBitsToFloat(bits);
    }

    private Number readFloat64() {
        ensureAvailable(8);
        long bits = 0;
        for (int i = 0; i < 8; i++) {
            bits = (bits << 8) | readUnsignedByte();
        }
        return Double.longBitsToDouble(bits);
    }

    private long readUInt(int byteLength) {
        ensureAvailable(byteLength);
        long value = 0;
        for (int i = 0; i < byteLength; i++) {
            value = (value << 8) | readUnsignedByte();
        }
        return value;
    }

    private Object readUInt64() {
        ensureAvailable(8);
        BigInteger value = BigInteger.ZERO;
        for (int i = 0; i < 8; i++) {
            value = value.shiftLeft(8).or(BigInteger.valueOf(readUnsignedByte()));
        }
        BigInteger max = BigInteger.valueOf(Long.MAX_VALUE);
        if (value.compareTo(max) <= 0) {
            return value.longValue();
        }
        return value.toString();
    }

    private long readInt(int byteLength) {
        ensureAvailable(byteLength);
        long value = 0;
        for (int i = 0; i < byteLength; i++) {
            value = (value << 8) | readUnsignedByte();
        }
        long shift = 64 - byteLength * 8;
        return (value << shift) >> shift; // sign extend
    }

    private long readInt64() {
        ensureAvailable(8);
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | readUnsignedByte();
        }
        return value;
    }

    private int readByte() {
        return buffer[offset++];
    }

    private int readUnsignedByte() {
        return buffer[offset++] & 0xFF;
    }

    private void ensureAvailable(int bytes) {
        if (offset + bytes > buffer.length) {
            throw new MtrMessagePackException("Unexpected end of buffer (" + bytes + " requested)");
        }
    }

    public static final class MtrMessagePackException extends RuntimeException {
        private MtrMessagePackException(String message) {
            super(message);
        }
    }
}
