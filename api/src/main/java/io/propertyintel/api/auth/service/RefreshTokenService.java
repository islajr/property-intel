package io.propertyintel.api.auth.service;

import io.propertyintel.api.auth.entity.RefreshToken;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.repository.RefreshTokenRepository;
import io.propertyintel.api.global.exception.exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${refreshtoken.expiry.seconds}")
    private Long expiry;

    @Transactional
    public String generateRefreshToken(User user) {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String hashedToken = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hashedToken)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(expiry))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    public RefreshToken validateToken(String rawToken) {

        /*
        * Returns a RefreshToken object depending on if the provided refresh token is valid or not
        * Is typically only called on refresh attempts. Failure should ideally result in new login prompts
        * */

        // First hash raw token in preparation for validation
        String hashedToken = hashToken(rawToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        // Check for equality
        if (!MessageDigest.isEqual(
                hashedToken.getBytes(StandardCharsets.UTF_8),
                storedToken.getTokenHash().getBytes(StandardCharsets.UTF_8)
        )) {
            log.debug("Refresh token does not match storedToken for user: {}", storedToken.getUser().getEmail());
            throw new UnauthorizedException("Refresh token does not match storedToken");
        }

        // Check if token has been revoked
        if (storedToken.getIsRevoked()) {
            log.debug("Refresh token is revoked for user: {}", storedToken.getUser().getEmail());
            throw new UnauthorizedException("Refresh token is revoked for user");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            log.debug("Refresh token is expired for user: {}", storedToken.getUser().getEmail());
            throw new UnauthorizedException("Refresh token is expired for user");
        }

        return storedToken;
    }

    @Transactional
    public void revokeToken(String rawToken) {

        /*
        * Revokes a token upon logout. The presence of a logged-in user suggests the presence of a refresh token.
        * Hence, tokens must be properly revoked whenever this method is called.
        * */

        String hashedToken = hashToken(rawToken);

        refreshTokenRepository.findByTokenHash(hashedToken)
                .ifPresent(refreshToken -> {
                    refreshToken.setIsRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                    log.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
                });
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            // Convert the byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            log.debug("Error while hashing token: Algorithm does not exist", e);
            throw new RuntimeException("Failed to validate token", e);
        }
    }

}
