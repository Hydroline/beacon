package com.hydroline.beacon.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Minimal NBT reader that supports common tag types and converts to simple Java Maps/Lists for JSON serialization.
 * This avoids depending on NMS or external libraries.
 */
public class NbtUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Map<String, Object> readPlayerDatToMap(InputStream gzippedInput) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(gzippedInput);
             DataInputStream in = new DataInputStream(gis)) {
            int type = in.readUnsignedByte();
            if (type != 10) { // TAG_Compound
                throw new IOException("Invalid root tag type: " + type);
            }
            // root name (often empty), read and discard
            readString(in);
            return readCompoundPayload(in);
        }
    }

    public static String toJson(Map<String, Object> map) throws IOException {
        return MAPPER.writeValueAsString(map);
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static Map<String, Object> readCompoundPayload(DataInputStream in) throws IOException {
        Map<String, Object> map = new HashMap<>();
        while (true) {
            int type;
            try {
                type = in.readUnsignedByte();
            } catch (EOFException eof) {
                // Unexpected EOF
                throw eof;
            }
            if (type == 0) { // TAG_End
                break;
            }
            String name = readString(in);
            Object value = readPayloadByType(in, type);
            map.put(name, value);
        }
        return map;
    }

    private static Object readPayloadByType(DataInputStream in, int type) throws IOException {
        switch (type) {
            case 1: // byte
                return in.readByte();
            case 2: // short
                return in.readShort();
            case 3: // int
                return in.readInt();
            case 4: // long
                return in.readLong();
            case 5: // float
                return in.readFloat();
            case 6: // double
                return in.readDouble();
            case 7: { // byte array
                int length = in.readInt();
                List<Integer> arr = new ArrayList<>(Math.max(0, Math.min(length, 1 << 20)));
                for (int i = 0; i < length; i++) {
                    arr.add((int) in.readByte());
                }
                return arr;
            }
            case 8: // string
                return readString(in);
            case 9: { // list
                int elemType = in.readUnsignedByte();
                int length = in.readInt();
                List<Object> list = new ArrayList<>(Math.max(0, Math.min(length, 1 << 20)));
                for (int i = 0; i < length; i++) {
                    list.add(readPayloadByType(in, elemType));
                }
                return list;
            }
            case 10: // compound
                return readCompoundPayload(in);
            case 11: { // int array
                int length = in.readInt();
                List<Integer> arr = new ArrayList<>(Math.max(0, Math.min(length, 1 << 20)));
                for (int i = 0; i < length; i++) arr.add(in.readInt());
                return arr;
            }
            case 12: { // long array
                int length = in.readInt();
                List<Long> arr = new ArrayList<>(Math.max(0, Math.min(length, 1 << 20)));
                for (int i = 0; i < length; i++) arr.add(in.readLong());
                return arr;
            }
            default:
                throw new IOException("Unsupported NBT tag type: " + type);
        }
    }
}
