package io.propertyintel.api.auth.config;

import io.propertyintel.api.auth.entity.UserPrincipal;
import io.propertyintel.api.auth.service.JwtService;
import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver resolver;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludedPaths = Arrays.asList(
            "/api/v1/auth/login",
            "/auth/login",
            "/api/v1/auth/register",
            "/auth/register",
            "/api/v1/auth/refresh",
            "/auth/refresh",
            "/api/v1/auth/verify-email",
            "/auth/verify-email",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/**/swagger-ui.html",
            "/webjars/**",
            "/api/v1/listings/**",
            "/listings/**",
            "/api/v1/market/**",
            "/market/**",
            "/api/v1/search",
            "/search"
    );

    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String authPrefix = "Bearer ";

            log.debug("Intercepting request for path: {} with JWTFilter", request.getServletPath());

            // Check for auth header
            if (authHeader == null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!authHeader.startsWith(authPrefix)) {
                log.warn("Access denied. Authorization header is incorrect on path: {}", request.getServletPath());
                throw new BadRequestException("Authorization header is incorrect");
            }

            String token = authHeader.substring(authPrefix.length());
            String email = jwtService.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // if credentials are valid and user is unauthenticated
                UserPrincipal user = (UserPrincipal) userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(token, user)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Request authenticated successfully. User principal set for: {}", email);
                }
            }
        } catch (Exception ex) {
            log.warn("JWT authorization verification failed for path: {} - Reason: {}", request.getServletPath(), ex.getMessage());
            resolver.resolveException(request, response, null, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String currPath = request.getServletPath();
        boolean matchesExclusion = excludedPaths.stream()
                .anyMatch(path -> pathMatcher.match(path, currPath));
        if (matchesExclusion) {
            log.debug("Bypassing JWT filter for excluded path: {}", currPath);
        }
        return matchesExclusion;
    }
}
