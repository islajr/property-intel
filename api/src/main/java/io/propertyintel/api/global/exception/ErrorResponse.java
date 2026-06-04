package io.propertyintel.api.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Standard API error response structure")
public record ErrorResponse(

        @Schema(description = "Error classification or HTTP Status name", example = "BAD_REQUEST")
        String error,

        @Schema(description = "Detailed explanation of the error", example = "Validation failed for fields: ...")
        String message,

        @Schema(description = "The requested URI path that resulted in the error", example = "/api/v1/auth/login")
        String path,

        @Schema(description = "ISO-8601 timestamp of when the error occurred", example = "2026-06-04T18:44:06Z")
        String timestamp
) {
    public ErrorResponse(String error, String message, String path, Instant timestamp) {
        this(error, message, path, timestamp.toString());
    }
}
