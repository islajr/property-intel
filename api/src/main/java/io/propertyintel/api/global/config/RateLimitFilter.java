package io.propertyintel.api.global.config;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.ConsumptionProbe;
import io.propertyintel.api.global.service.RateLimitService;
import io.propertyintel.api.global.service.RateLimitService.Tier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Tier tier;
        String key;
        
        if (path.startsWith("/api/v1/auth")) {
            tier = Tier.AUTH;
            key = "auth:" + getClientIP(request);
        } else if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            tier = Tier.AUTHENTICATED;
            key = "user:" + auth.getName();
        } else {
            tier = Tier.PUBLIC;
            key = "public:" + getClientIP(request);
        }

        ConsumptionProbe probe = rateLimitService.tryConsume(key, tier);

        // Add headers to every response regardless of outcome
        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitService.getCapacityForTier(tier)));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000L));

        if (!probe.isConsumed()) {
            response.setStatus(429);
            response.getWriter().write("""
                    {
                        "error": "%s",
                        "message": "%s",
                        "path": "%s",
                        "timestamp": "%s"
                    }
                    """.formatted(
                    HttpStatus.TOO_MANY_REQUESTS.name(),
                    "Rate limit exceeded. Please try again later",
                    request.getRequestURI(),
                    Instant.now()
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
