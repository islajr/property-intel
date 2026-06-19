package io.propertyintel.api.auth.service;

import io.propertyintel.api.auth.entity.RefreshToken;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.repository.RefreshTokenRepository;
import io.propertyintel.api.global.exception.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "expiry", 3600L);

        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encoded")
                .role("USER")
                .isEmailVerified(true)
                .build();
    }

    @Test
    void testGenerateRefreshTokenForNewUser() {
        String rawToken = refreshTokenService.generateRefreshToken(sampleUser, true);

        assertNotNull(rawToken);
        assertFalse(rawToken.isEmpty());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).updateToken(any(RefreshToken.class));
    }

    @Test
    void testGenerateRefreshTokenForExistingUser() {
        String rawToken = refreshTokenService.generateRefreshToken(sampleUser, false);

        assertNotNull(rawToken);
        assertFalse(rawToken.isEmpty());
        verify(refreshTokenRepository).updateToken(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void testValidateTokenSuccess() {
        // We generate a token to compute its hash
        String rawToken = refreshTokenService.generateRefreshToken(sampleUser, true);
        
        // Retrieve the generated RefreshToken stored in DB (we mock it returning a matching hash)
        // RefreshTokenService computes hex string of SHA-256 for the base64 raw token
        String expectedHash = ReflectionTestUtils.invokeMethod(refreshTokenService, "hashToken", rawToken);

        RefreshToken storedToken = RefreshToken.builder()
                .tokenHash(expectedHash)
                .user(sampleUser)
                .isRevoked(false)
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        when(refreshTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(storedToken));

        RefreshToken validated = refreshTokenService.validateToken(rawToken);

        assertNotNull(validated);
        assertEquals(expectedHash, validated.getTokenHash());
        assertFalse(validated.getIsRevoked());
    }

    @Test
    void testValidateTokenExpiredThrowsUnauthorized() {
        String rawToken = "sample-raw-token";
        String expectedHash = ReflectionTestUtils.invokeMethod(refreshTokenService, "hashToken", rawToken);

        RefreshToken expiredToken = RefreshToken.builder()
                .tokenHash(expectedHash)
                .user(sampleUser)
                .isRevoked(false)
                .expiresAt(Instant.now().minusSeconds(10)) // Expired
                .build();

        when(refreshTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(expiredToken));

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.validateToken(rawToken));
    }

    @Test
    void testValidateTokenRevokedThrowsUnauthorized() {
        String rawToken = "sample-raw-token";
        String expectedHash = ReflectionTestUtils.invokeMethod(refreshTokenService, "hashToken", rawToken);

        RefreshToken revokedToken = RefreshToken.builder()
                .tokenHash(expectedHash)
                .user(sampleUser)
                .isRevoked(true) // Revoked
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        when(refreshTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(revokedToken));

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.validateToken(rawToken));
    }
}
