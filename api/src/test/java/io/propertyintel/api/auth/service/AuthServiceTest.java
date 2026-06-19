package io.propertyintel.api.auth.service;

import io.propertyintel.api.auth.dto.AuthResult;
import io.propertyintel.api.auth.dto.LoginRequest;
import io.propertyintel.api.auth.dto.RegisterRequest;
import io.propertyintel.api.auth.entity.RefreshToken;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.UserPrincipal;
import io.propertyintel.api.auth.repository.UserRepository;
import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import io.propertyintel.api.global.exception.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private UserPrincipal samplePrincipal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpirySeconds", 900);

        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .role("USER")
                .isEmailVerified(false)
                .build();

        samplePrincipal = new UserPrincipal(sampleUser);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(samplePrincipal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(sampleUser)).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(sampleUser, false)).thenReturn("refresh-token");

        AuthResult result = authService.login(request);

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(900, result.expiresIn());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(sampleUser);
        verify(refreshTokenService).generateRefreshToken(sampleUser, false);
    }

    @Test
    void testLoginUnauthenticated() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong");
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(any(User.class), eq(true))).thenReturn("refresh-token");

        AuthResult result = authService.register(request);

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(900, result.expiresIn());

        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        verify(refreshTokenService).generateRefreshToken(any(User.class), eq(true));
    }

    @Test
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRefreshSuccess() {
        RefreshToken mockRefreshToken = new RefreshToken("tokenHash", sampleUser, false, Instant.now(), Instant.now().plusSeconds(3600));
        when(refreshTokenService.validateToken("refresh-token")).thenReturn(mockRefreshToken);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(samplePrincipal);
        when(jwtService.generateToken(sampleUser)).thenReturn("new-access-token");
        when(refreshTokenService.generateRefreshToken(sampleUser, false)).thenReturn("new-refresh-token");

        AuthResult result = authService.refresh("refresh-token");

        assertNotNull(result);
        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());
        assertEquals(900, result.expiresIn());

        verify(refreshTokenService).validateToken("refresh-token");
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(jwtService).generateToken(sampleUser);
        verify(refreshTokenService).generateRefreshToken(sampleUser, false);
    }

    @Test
    void testRefreshNullOrEmpty() {
        assertThrows(UnauthorizedException.class, () -> authService.refresh(null));
        assertThrows(UnauthorizedException.class, () -> authService.refresh(""));
    }
}
