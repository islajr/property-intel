package io.propertyintel.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload to resend user verification e-mail")
public record ResendVerificationRequest(
        @NotBlank(message = "E-mail field cannot be blank")
        @NotNull(message = "E-mail field cannot be null")
        @Email(message = "Please input a valid e-mail address")
        @Schema(description = "User e-maik", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email
) {
}
