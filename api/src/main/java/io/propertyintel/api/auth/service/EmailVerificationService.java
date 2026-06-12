package io.propertyintel.api.auth.service;

import io.propertyintel.api.auth.entity.EmailVerificationToken;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.enums.UserStatus;
import io.propertyintel.api.auth.repository.EmailTokenRepository;
import io.propertyintel.api.auth.repository.UserRepository;
import io.propertyintel.api.global.exception.exceptions.EmailVerificationException;
import io.propertyintel.api.global.exception.exceptions.EmailVerificationException.Reason;
import io.propertyintel.api.global.resend.service.ResendEmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static io.propertyintel.api.global.util.SecurityUtils.hashToken;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {

    private final EmailTokenRepository emailTokenRepository;
    private final UserRepository userRepository;
    private final ResendEmailService resendEmailService;

    private static final int RESEND_COOLDOWN_MINUTES = 2;

    @Value("${email.confirmation.expiryHours}")
    private int expiryHours;

    @Value("${application.baseURL}")
    private String baseURL;


    /*
     * Issues a token and dispatches the e-mail asynchronously
     * */
    @Async("emailTaskExecutor")
    public void issueAndSendVerificationEmail(User user) {

        log.info("Started e-mail verification job for user: {}", user.getEmail());
        emailTokenRepository.invalidateTokensForUser(user, Instant.now());

        String rawToken = generateSecureToken();

        String hashedToken = hashToken(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .tokenHash(hashedToken)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(expiryHours, ChronoUnit.HOURS))
                .build();

        emailTokenRepository.save(verificationToken);

        log.debug("Generated e-mail verification token for user: {}", user.getEmail());

        String verificationURL = baseURL + "/api/v1/auth/verify-email?token=" + rawToken;   // TODO: refine verification URL for prod.

        resendEmailService.sendVerificationEmail(user.getEmail(), verificationURL);

    }

    /*
    * Validates the token and activates user account
    * Returns the verified e-mail address upon success
    * */
    public String verifyToken(String rawToken) {
        String hashedToken = hashToken(rawToken);
        EmailVerificationToken storedToken = emailTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new EmailVerificationException("Invalid verification link"));

        if (storedToken.isUsed()){
            log.warn("Email verification request contains already-used link");
            throw new EmailVerificationException("Provided link has already been used", Reason.ALREADY_USED);
        }

        if (storedToken.isExpired()) {
            log.warn("Email verification request contains expired link");
            throw new EmailVerificationException("Provided link has expired", Reason.EXPIRED);
        }

        if(!storedToken.isValid()) {
            log.warn("Email verification request contained invalid link");
            throw new EmailVerificationException("Provided link is invalid", Reason.GENERIC);
        }

        // If token is valid and proper
        storedToken.setUsed(true);
        storedToken.setUsedAt(Instant.now());
        emailTokenRepository.save(storedToken);

        // Activate user
        User user = storedToken.getUser();

        if (user.getIsEmailVerified()) {
            log.warn("User: {} is already verified", user.getEmail());
            throw new EmailVerificationException("User is already verified");
        }

        user.setIsEmailVerified(true);
        user.setUserStatus(UserStatus.ACTIVE);
        // Since 'User' is managed by JPA within this instance, it is saved automatically as long as @Transactional is deployed

        return user.getEmail();

    }

    /*
    * Resend flow for re-sending confirmation e-mails
    * Enforces a cooldown to prevent abuse
    * */
    @Async("emailTaskExecutor")
    public void resendVerificationEmail(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new EmailVerificationException("Provided e-mail address does not exist"));

        if (user.getUserStatus() == UserStatus.ACTIVE) throw new EmailVerificationException("User is already verified");

        Instant cooldownPeriod = Instant.now().minus(RESEND_COOLDOWN_MINUTES, ChronoUnit.MINUTES);

        long recentCount = emailTokenRepository.countRecentTokens(user, cooldownPeriod);

        if (recentCount > 0) {
            throw new EmailVerificationException(
                    "Please wait %d minutes before requesting another email".formatted(RESEND_COOLDOWN_MINUTES),
                    Reason.TOO_MANY_REQUESTS);
        }

        // Issue and deploy new e-mail
        issueAndSendVerificationEmail(user);

    }

    /*
    * Securely generate token for e-mail verification
    * */
    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
