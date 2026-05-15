package io.propertyintel.api.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
