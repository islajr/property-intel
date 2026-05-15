package io.propertyintel.api.auth.dto;

public record RegisterRequest(
        String email,
        String password
) {
}
