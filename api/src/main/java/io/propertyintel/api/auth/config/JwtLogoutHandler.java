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
    private final JwtService jwtService;
    private final UserDetailService userDetailsService;


    @Transactional
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) {
        /*
         * Invalidates refresh tokens and logs user out effectively
         * */


        // Checks to properly validate signed-in user before logout procedure

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing Access Token");
        }

        String accessToken = authHeader.substring(7);
        String email = jwtService.extractEmail(accessToken);
        UserPrincipal user = (UserPrincipal) userDetailsService.loadUserByUsername(email);

        // Upon access failure, client-side should try a quick refresh
        if (!jwtService.isTokenValid(accessToken, user)) throw new UnauthorizedException("Invalid or Expired Access token");

        String refreshToken = extractRefreshToken(request);

        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
            log.debug("Revoked refresh token");

            ResponseCookie deleteCookie = ResponseCookie.from(
                            "refreshToken"
                    )
                    .httpOnly(true)
                    .secure(true)
                    .path("/api/v1/auth")
                    .maxAge(0)
                    .build();

            log.info("Deleted refresh token cookie");

            response.addHeader(
                    HttpHeaders.SET_COOKIE, deleteCookie.toString()
            );
        } else {
            log.debug("No refresh token found");
            throw new UnauthorizedException("No refresh token cookie found");
        }

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
        if (request.getCookies() == null) log.warn("Request cookies empty. Failed to obtain refresh token`");

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshToken")) return cookie.getValue();
        }

        return null;
    }
}
