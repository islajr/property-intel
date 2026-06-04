package io.propertyintel.api.auth.dto;

public record AuthResult(
        String accessToken,
        String refreshToken,
        Integer expiresIn
) {
}
