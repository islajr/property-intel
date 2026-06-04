package io.propertyintel.api.auth.config;

import io.propertyintel.api.auth.entity.UserPrincipal;
import io.propertyintel.api.auth.service.JwtService;
import io.propertyintel.api.auth.service.RefreshTokenService;
import io.propertyintel.api.auth.service.UserDetailService;
import io.propertyintel.api.global.exception.exceptions.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.HandlerExceptionResolver;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtLogoutHandler implements LogoutHandler {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserDetailService userDetailsService;
    private final HandlerExceptionResolver resolver;

    public JwtLogoutHandler(
            RefreshTokenService refreshTokenService,
            JwtService jwtService,
            UserDetailService userDetailsService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.resolver = resolver;
    }

    @Transactional
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) {
        try {
            log.info("Processing logout request for user.");

            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Logout request failed. Authorization header is missing or incorrect.");
                throw new UnauthorizedException("Missing Access Token");
            }

            String accessToken = authHeader.substring(7);
            String email = jwtService.extractEmail(accessToken);
            UserPrincipal user = (UserPrincipal) userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(accessToken, user)) {
                log.warn("Logout request failed. Provided access token is invalid or expired for user: {}", email);
                throw new UnauthorizedException("Invalid or Expired Access token");
            }

            String refreshToken = extractRefreshToken(request);

            if (refreshToken != null) {
                refreshTokenService.revokeToken(refreshToken);
                log.debug("Successfully revoked refresh token database entry for user: {}", email);

                ResponseCookie deleteCookie = ResponseCookie.from("refreshToken")
                        .httpOnly(true)
                        .secure(true)
                        .path("/api/v1/auth")
                        .maxAge(0)
                        .build();

                response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
                log.info("Successfully deleted refresh token cookie for user: {}", email);
            } else {
                log.warn("Logout request failed. No refresh token cookie was found for user: {}", email);
                throw new UnauthorizedException("No refresh token cookie found");
            }

            // Invalidate HttpSessions
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                log.debug("Successfully invalidated HTTP session for user: {}", email);
            }

            // Clear security context
            SecurityContextHolder.clearContext();
            log.info("Successfully cleared security context. Logout complete for user: {}", email);
        } catch (Exception ex) {
            log.error("Error occurred during logout request resolution: {}", ex.getMessage());
            resolver.resolveException(request, response, null, ex);
        }
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.warn("Request cookies array is null. Failed to obtain refresh token.");
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken")) {
                return cookie.getValue();
            }
        }

        log.debug("No cookie named 'refreshToken' was found in request.");
        return null;
    }
}
