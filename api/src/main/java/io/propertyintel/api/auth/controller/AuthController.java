package io.propertyintel.api.auth.controller;

import io.propertyintel.api.auth.dto.*;
import io.propertyintel.api.auth.service.AuthService;
import io.propertyintel.api.auth.service.EmailVerificationService;
import io.propertyintel.api.global.exception.ErrorResponse;
import io.propertyintel.api.global.exception.exceptions.EmailVerificationException;
import io.propertyintel.api.global.exception.exceptions.RateLimitException;
import io.propertyintel.api.global.idempotency.annotation.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Operations", description = "Provides signup, login, and token refresh capabilities")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

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
        log.info("REST request to login user: {}", loginRequest.email());
        AuthResult result = authService.login(loginRequest);
        ResponseCookie cookie = createCookieToken(result.refreshToken());
        log.info("Successfully completed login REST endpoint for user: {}", loginRequest.email());
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
        log.info("REST request to register user: {}", registerRequest.email());
        AuthResult result = authService.register(registerRequest);
        ResponseCookie cookie = createCookieToken(result.refreshToken());
        log.info("Successfully completed register REST endpoint for user: {}", registerRequest.email());
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
        log.info("REST request to refresh access token using cookie.");
        AuthResult result = authService.refresh(refreshToken);
        ResponseCookie cookie = createCookieToken(result.refreshToken());
        log.info("Successfully completed refresh token REST endpoint.");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(result.accessToken(), result.expiresIn()));
    }

    @Operation(summary = "Verifies a user's e-mail address",
            description = "Verifies legitimacy of provided token in order to activate user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully verified and activated user",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Invalid or Expired token, or User already verified",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @Parameter(description = "Token provided and sent to user's e-mail address",
            example = "ejxiaewm2lJlwo_23z")
            @RequestParam String token) {
        log.info("REST request to verify e-mail address");
        String email = emailVerificationService.verifyToken(token);

        log.info("Successful e-mail verification attempt from user: {}", email);

        // TODO: Redirect to frontend success page upon success

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Resends a Verification e-mail to verify a user's account",
            description = "Verifies legitimacy of provided token in order to activate user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully resent verification e-mail",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Invalid e-mail address, or User already verified",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/resend-verification")
    @Idempotent
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        try {
            emailVerificationService.resendVerificationEmail(request.email());
            return ResponseEntity.ok(null);

        } catch(EmailVerificationException ex) {
            if (ex.getReason().equals(EmailVerificationException.Reason.TOO_MANY_REQUESTS)) {
                throw new RateLimitException(ex.getMessage());
            }

            throw new BadCredentialsException(ex.getMessage());
        }
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