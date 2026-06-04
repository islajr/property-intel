package io.propertyintel.api.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.UserPrincipal;
import io.propertyintel.api.auth.util.RSAKeyProperties;
import io.propertyintel.api.global.exception.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    @Value("${accesstoken.expiry.milliseconds}")
    private Long expiryMilliseconds;

    public JwtService(RSAKeyProperties keys) {
        this.privateKey = keys.getPrivateKey();
        this.publicKey = keys.getPublicKey();
    }

    public String generateToken(User user) {
        log.info("Generating JWT access token for user: {}", user.getEmail());
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + expiryMilliseconds)
                )
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String extractEmail(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (JwtException ex) {
            log.warn("Failed to extract email from JWT token. Reason: {}", ex.getMessage());
            throw new UnauthorizedException("Invalid or Expired access token");
        }
    }

    public boolean isTokenValid(String token, UserPrincipal user) {
        boolean emailMatch = extractEmail(token).equals(user.getUsername());
        boolean expired = isTokenExpired(token);
        log.debug("Verifying JWT token validity. Email Match: {}, Is Expired: {}", emailMatch, expired);
        return emailMatch && !expired;
    }

    private boolean isTokenExpired(String token) {
        try {
            boolean isExpired = extractClaims(token).getExpiration().before(new Date());
            if (isExpired) {
                log.warn("JWT token has expired.");
            }
            return isExpired;
        } catch (JwtException ex) {
            log.warn("Failed to check JWT token expiration status. Reason: {}", ex.getMessage());
            throw new UnauthorizedException("Invalid or Expired access token");
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
