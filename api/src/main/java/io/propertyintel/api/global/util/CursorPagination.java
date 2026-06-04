package io.propertyintel.api.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class CursorPagination {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String encode(Long id) {
        if (id == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(Map.of("id", id));
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encode cursor", e);
        }
    }

    public static Long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);
            Map<?, ?> map = objectMapper.readValue(json, Map.class);
            Number id = (Number) map.get("id");
            return id != null ? id.longValue() : null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid pagination cursor", e);
        }
    }

    public static String encodeMarket(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(Map.of("id", id));
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encode cursor", e);
        }
    }

    public static String decodeMarket(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);
            Map<?, ?> map = objectMapper.readValue(json, Map.class);
            String id = (String) map.get("id");
            return id != null ? id : null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid pagination cursor", e);
        }
    }
}
