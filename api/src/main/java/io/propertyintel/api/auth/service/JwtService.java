package io.propertyintel.api.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.UserPrincipal;
import io.propertyintel.api.auth.util.RSAKeyProperties;
import io.propertyintel.api.global.exception.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Service
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
            throw new UnauthorizedException("Invalid or Expired access token");
        }
    }

    public boolean isTokenValid(String token, UserPrincipal user) {
        return extractEmail(token).equals(user.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (JwtException ex) {
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
