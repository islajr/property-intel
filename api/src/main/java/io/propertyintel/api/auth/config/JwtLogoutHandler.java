package io.propertyintel.api.auth.config;

import io.propertyintel.api.auth.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtLogoutHandler implements LogoutHandler {

    private final RefreshTokenService refreshTokenService;


    @Transactional
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) {
        /*
         * Invalidates refresh tokens and logs user out effectively
         * */

        String refreshToken = extractRefreshToken(request);

        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
            log.debug("Revoked refresh token");

            ResponseCookie deleteCookie = ResponseCookie.from(
                            "refreshToken"
                    )
                    .httpOnly(true)
                    .secure(true)
                    .path("/api/v1/auth/refresh")
                    .maxAge(0)
                    .build();

            log.info("Deleted refresh token cookie");

            response.addHeader(
                    HttpHeaders.SET_COOKIE, deleteCookie.toString()
            );
        } else log.warn("No refresh token found");

        // Invalidate HttpSessions
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.debug("Invalidated session");
        }

        // Clear security context
        SecurityContextHolder.clearContext();
        log.debug("Cleared security context");
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshToken")) return cookie.getValue();
        }

        return null;
    }
}
