package io.propertyintel.api.auth.config;

import io.propertyintel.api.auth.entity.UserPrincipal;
import io.propertyintel.api.auth.service.JwtService;
import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludedPaths = Arrays.asList(
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/**/swagger-ui.html",
            "/webjars/**",
            "/api/v1/listings/**",
            "/api/v1/market/**",
            "/api/v1/search"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /*
        * JwtFilter is invoked by the SecurityFilterChain. It:
        *   - reads the authorization header
        *   - extracts token
        *   - validates token
        *   - authenticates user
        * */


        final String authHeader = request.getHeader("Authorization");
        final String authPrefix = "Bearer ";

        if (authHeader == null || !authHeader.startsWith(authPrefix)) {
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
            }
        }

        filterChain.doFilter(request, response);

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String currPath = request.getServletPath();

        return excludedPaths.stream()
                .anyMatch(path -> pathMatcher.match(path, currPath));
    }
}
