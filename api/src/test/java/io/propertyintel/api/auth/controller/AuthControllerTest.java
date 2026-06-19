package io.propertyintel.api.auth.controller;

import io.propertyintel.api.auth.dto.AuthResponse;
import io.propertyintel.api.auth.dto.AuthResult;
import io.propertyintel.api.auth.dto.LoginRequest;
import io.propertyintel.api.auth.dto.RegisterRequest;
import io.propertyintel.api.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "refreshTokenExpirySeconds", 604800L);
    }

    @Test
    void testLogin() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        AuthResult serviceResult = new AuthResult("access-token", "refresh-token", 900);
        when(authService.login(request)).thenReturn(serviceResult);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("access-token", body.accessToken());
        assertEquals(900, body.expiresIn());

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("refreshToken=refresh-token"));
        assertTrue(setCookieHeader.contains("HttpOnly"));
        assertTrue(setCookieHeader.contains("Secure"));
        assertTrue(setCookieHeader.contains("SameSite=Strict"));
        assertTrue(setCookieHeader.contains("Max-Age=604800"));

        verify(authService).login(request);
    }

    @Test
    void testRegister() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password");
        AuthResult serviceResult = new AuthResult("access-token", "refresh-token", 900);
        when(authService.register(request)).thenReturn(serviceResult);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("access-token", body.accessToken());
        assertEquals(900, body.expiresIn());

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("refreshToken=refresh-token"));

        verify(authService).register(request);
    }

    @Test
    void testRefresh() {
        AuthResult serviceResult = new AuthResult("new-access-token", "new-refresh-token", 900);
        when(authService.refresh("old-refresh-token")).thenReturn(serviceResult);

        ResponseEntity<AuthResponse> response = authController.refresh("old-refresh-token");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("new-access-token", body.accessToken());
        assertEquals(900, body.expiresIn());

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("refreshToken=new-refresh-token"));

        verify(authService).refresh("old-refresh-token");
    }
}
