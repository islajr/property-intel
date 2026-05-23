package io.propertyintel.api.global.exception;

import java.time.Instant;

public record ErrorResponse(

        String error,
        String message,
        String path,
        String timestamp
) {
    public ErrorResponse(String error, String message, String path, Instant timestamp) {
        this(error, message, path, timestamp.toString());
    }
}
