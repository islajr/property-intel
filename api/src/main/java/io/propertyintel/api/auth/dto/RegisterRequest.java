package io.propertyintel.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(

        @Email(message = "Please input a valid e-mail address")
        @NotNull(message = "E-mail field cannot be null")
        @NotBlank(message = "E-mail field cannot be blank")
        String email,

        @NotNull(message = "Password field cannot be null")
        @NotBlank(message = "Password field cannot be blank")
        String password
) {
}
