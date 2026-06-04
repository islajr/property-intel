package io.propertyintel.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing access token information")
public record AuthResponse(
        @Schema(description = "JWT Access Token for authenticating subsequent requests", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Access token lifetime duration in seconds", example = "900")
        Integer expiresIn
) {
}
