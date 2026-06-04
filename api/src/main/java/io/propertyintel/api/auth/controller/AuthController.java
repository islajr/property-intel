package io.propertyintel.api.auth.controller;

import io.propertyintel.api.auth.dto.AuthResponse;
import io.propertyintel.api.auth.dto.AuthResult;
import io.propertyintel.api.auth.dto.LoginRequest;
import io.propertyintel.api.auth.dto.RegisterRequest;
import io.propertyintel.api.auth.service.AuthService;
import io.propertyintel.api.global.idempotency.annotation.Idempotent;
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
public class AuthController {

    private final AuthService authService;

    @Value("${refreshtoken.expiry.seconds}")
    private Long refreshTokenExpirySeconds;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResult result = authService.login(loginRequest);
        ResponseCookie cookie = createCookieToken(result.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(result.accessToken(), result.expiresIn()));
    }

    @PostMapping("/register")
    @Idempotent
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResult result = authService.register(registerRequest);
        ResponseCookie cookie = createCookieToken(result.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(result.accessToken(), result.expiresIn()));
    }

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