package io.propertyintel.api.global.exception;

import java.time.LocalDateTime;

public record ErrorResponse(

        String error,
        String message,
        String path,
        String timestamp
) {
    public ErrorResponse(String error, String message, String path, LocalDateTime timestamp) {
        this(error, message, path, timestamp.toString());
    }
}
