package io.propertyintel.api.auth.controller;

import io.propertyintel.api.auth.dto.AuthResponse;
import io.propertyintel.api.auth.dto.LoginRequest;
import io.propertyintel.api.auth.dto.RegisterRequest;
import io.propertyintel.api.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue String refreshToken) {
        return authService.refresh(refreshToken);
    }

}