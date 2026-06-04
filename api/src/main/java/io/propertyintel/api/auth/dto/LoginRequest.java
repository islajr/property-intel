package io.propertyintel.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for user authentication")
public record LoginRequest(
        @Schema(description = "User e-mail address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @Email(message = "Please input a valid e-mail address")
        @NotNull(message = "E-mail field cannot be null")
        @NotBlank(message = "E-mail field cannot be blank")
        String email,

        @Schema(description = "User password", example = "SecretPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Password field cannot be null")
        @NotBlank(message = "Password field cannot be blank")
        String password
) {
}
