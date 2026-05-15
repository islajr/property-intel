package io.propertyintel.api.auth.dto;

public record AuthResponse(
        String accessToken,
        Integer expiresIn
) {
}
