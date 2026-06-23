package io.propertyintel.api.auth.controller;

import io.propertyintel.api.BaseControllerIntegrationTest;
import io.propertyintel.api.auth.dto.AuthResult;
import io.propertyintel.api.auth.dto.LoginRequest;
import io.propertyintel.api.auth.dto.RegisterRequest;
import io.propertyintel.api.auth.dto.ResendVerificationRequest;
import io.propertyintel.api.auth.service.AuthService;
import io.propertyintel.api.auth.service.EmailVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerIntegrationTest extends BaseControllerIntegrationTest {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @Autowired
    private AuthController authController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "refreshTokenExpirySeconds", 604800L);
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        AuthResult result = new AuthResult("access-token", "refresh-token", 900);

        when(authService.login(any(LoginRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(cookie().value("refreshToken", "refresh-token"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/v1/auth"));
    }

    @Test
    void testLoginValidationFailed() throws Exception {
        LoginRequest request = new LoginRequest("not-an-email", "");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("new@example.com", "password123");
        AuthResult result = new AuthResult("access-token", "refresh-token", 900);

        when(authService.register(any(RegisterRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void testRefreshSuccess() throws Exception {
        AuthResult result = new AuthResult("new-access-token", "new-refresh-token", 900);

        when(authService.refresh("my-refresh-token")).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", "my-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(cookie().value("refreshToken", "new-refresh-token"));
    }

    @Test
    void testVerifyEmailSuccess() throws Exception {
        when(emailVerificationService.verifyToken("token-123")).thenReturn("test@example.com");

        mockMvc.perform(get("/api/v1/auth/verify-email").param("token", "token-123"))
                .andExpect(status().isOk());
    }

    @Test
    void testResendVerificationSuccess() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest("test@example.com");

        doNothing().when(emailVerificationService).resendVerificationEmail("test@example.com");

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
