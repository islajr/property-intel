package io.propertyintel.api.auth.service;

import io.propertyintel.api.auth.entity.EmailVerificationToken;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.enums.UserStatus;
import io.propertyintel.api.auth.repository.EmailTokenRepository;
import io.propertyintel.api.auth.repository.UserRepository;
import io.propertyintel.api.global.exception.exceptions.EmailVerificationException;
import io.propertyintel.api.global.resend.service.ResendEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailTokenRepository emailTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResendEmailService resendEmailService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailVerificationService, "expiryHours", 24);
        ReflectionTestUtils.setField(emailVerificationService, "baseURL", "http://localhost:8080");

        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .email("verify@example.com")
                .password("password")
                .role("USER")
                .isEmailVerified(false)
                .userStatus(UserStatus.UNVERIFIED)
                .build();
    }

    @Test
    void testIssueAndSendVerificationEmail() {
        emailVerificationService.issueAndSendVerificationEmail(sampleUser);

        verify(emailTokenRepository).invalidateTokensForUser(eq(sampleUser), any(Instant.class));
        verify(emailTokenRepository).save(any(EmailVerificationToken.class));
        verify(resendEmailService).sendVerificationEmail(eq("verify@example.com"), anyString());
    }

    @Test
    void testVerifyTokenSuccess() {
        String rawToken = "raw-verification-token";
        String hashedToken = io.propertyintel.api.global.util.SecurityUtils.hashToken(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .tokenHash(hashedToken)
                .user(sampleUser)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .isUsed(false)
                .build();

        when(emailTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(verificationToken));

        String email = emailVerificationService.verifyToken(rawToken);

        assertEquals("verify@example.com", email);
        assertTrue(verificationToken.isUsed());
        assertNotNull(verificationToken.getUsedAt());
        assertTrue(sampleUser.getIsEmailVerified());
        assertEquals(UserStatus.ACTIVE, sampleUser.getUserStatus());

        verify(emailTokenRepository).save(verificationToken);
    }

    @Test
    void testVerifyTokenExpiredThrowsException() {
        String rawToken = "expired-token";
        String hashedToken = io.propertyintel.api.global.util.SecurityUtils.hashToken(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .tokenHash(hashedToken)
                .user(sampleUser)
                .createdAt(Instant.now().minus(48, ChronoUnit.HOURS))
                .expiresAt(Instant.now().minus(24, ChronoUnit.HOURS))
                .isUsed(false)
                .build();

        when(emailTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(verificationToken));

        assertThrows(EmailVerificationException.class, () -> emailVerificationService.verifyToken(rawToken));
    }

    @Test
    void testVerifyTokenAlreadyUsedThrowsException() {
        String rawToken = "used-token";
        String hashedToken = io.propertyintel.api.global.util.SecurityUtils.hashToken(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .tokenHash(hashedToken)
                .user(sampleUser)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .isUsed(true)
                .build();

        when(emailTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(verificationToken));

        assertThrows(EmailVerificationException.class, () -> emailVerificationService.verifyToken(rawToken));
    }
}
