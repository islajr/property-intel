package io.propertyintel.api.auth.controller;

import io.propertyintel.api.auth.dto.AuthResponse;
import io.propertyintel.api.auth.dto.AuthResult;
import io.propertyintel.api.auth.dto.LoginRequest;
import io.propertyintel.api.auth.dto.RegisterRequest;
import io.propertyintel.api.auth.service.AuthService;
import io.propertyintel.api.global.exception.ErrorResponse;
import io.propertyintel.api.global.idempotency.annotation.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Operations", description = "Provides signup, login, and token refresh capabilities")
public class AuthController {

    private final AuthService authService;

    @Value("${refreshtoken.expiry.seconds}")
    private Long refreshTokenExpirySeconds;

    @Operation(summary = "Authenticate user credentials",
            description = "Validates the user's email and password, returning an access token and setting a HttpOnly refresh token cookie on success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation errors",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResult result = authService.login(loginRequest);
        ResponseCookie cookie = createCookieToken(result.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(result.accessToken(), result.expiresIn()));
    }

    @Operation(summary = "Register a new user",
            description = "Creates a new user account with default role USER, returning an access token and setting a HttpOnly refresh token cookie on success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered and authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    @Idempotent
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResult result = authService.register(registerRequest);
        ResponseCookie cookie = createCookieToken(result.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(result.accessToken(), result.expiresIn()));
    }

    @Operation(summary = "Refresh access token",
            description = "Refreshes the user access token and rotates the refresh token using the HttpOnly refresh token cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access token successfully refreshed",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refreshToken") String refreshToken) {
        AuthResult result = authService.refresh(refreshToken);
        ResponseCookie cookie = createCookieToken(result.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(result.accessToken(), result.expiresIn()));
    }

    private ResponseCookie createCookieToken(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(refreshTokenExpirySeconds)
                .sameSite("Strict")
                .build();
    }
}