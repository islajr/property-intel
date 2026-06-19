package io.propertyintel.api.global.ratelimit.config;

import io.github.bucket4j.ConsumptionProbe;
import io.propertyintel.api.global.ratelimit.service.RateLimitService;
import io.propertyintel.api.global.ratelimit.service.RateLimitService.Tier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ConsumptionProbe consumptionProbe;

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    private StringWriter responseOut;

    @BeforeEach
    void setUp() throws Exception {
        responseOut = new StringWriter();
        PrintWriter writer = new PrintWriter(responseOut);
        lenient().when(response.getWriter()).thenReturn(writer);
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void testFilterAllowsRequestWhenTokensAvailable() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/listings");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(consumptionProbe.isConsumed()).thenReturn(true);
        when(consumptionProbe.getRemainingTokens()).thenReturn(9L);
        when(consumptionProbe.getNanosToWaitForRefill()).thenReturn(0L);

        when(rateLimitService.tryConsume(eq("public:127.0.0.1"), eq(Tier.PUBLIC))).thenReturn(consumptionProbe);
        when(rateLimitService.getCapacityForTier(Tier.PUBLIC)).thenReturn(10L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-RateLimit-Limit", "10");
        verify(response).setHeader("X-RateLimit-Remaining", "9");
        verify(response).setHeader("X-RateLimit-Reset", "0");
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void testFilterBlocksRequestWhenLimitExceeded() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/listings");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(consumptionProbe.isConsumed()).thenReturn(false);
        when(consumptionProbe.getRemainingTokens()).thenReturn(0L);
        when(consumptionProbe.getNanosToWaitForRefill()).thenReturn(5_000_000_000L); // 5 seconds

        when(rateLimitService.tryConsume(eq("public:127.0.0.1"), eq(Tier.PUBLIC))).thenReturn(consumptionProbe);
        when(rateLimitService.getCapacityForTier(Tier.PUBLIC)).thenReturn(10L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-RateLimit-Limit", "10");
        verify(response).setHeader("X-RateLimit-Remaining", "0");
        verify(response).setHeader("X-RateLimit-Reset", "5");
        verify(response).setStatus(429);
        verifyNoInteractions(filterChain);
        
        assertTrue(responseOut.toString().contains("Rate limit exceeded"));
    }

    @Test
    void testFilterResolvesAuthTierForAuthEndpoint() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");

        when(consumptionProbe.isConsumed()).thenReturn(true);
        when(consumptionProbe.getRemainingTokens()).thenReturn(4L);
        when(consumptionProbe.getNanosToWaitForRefill()).thenReturn(0L);

        when(rateLimitService.tryConsume(eq("auth:192.168.0.1"), eq(Tier.AUTH))).thenReturn(consumptionProbe);
        when(rateLimitService.getCapacityForTier(Tier.AUTH)).thenReturn(5L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimitService).tryConsume("auth:192.168.0.1", Tier.AUTH);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterResolvesAuthenticatedTierForLoggedInUser() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/listings");

        // Mock authentication context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("userPrincipal");
        when(authentication.getName()).thenReturn("john@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(consumptionProbe.isConsumed()).thenReturn(true);
        when(consumptionProbe.getRemainingTokens()).thenReturn(99L);
        when(consumptionProbe.getNanosToWaitForRefill()).thenReturn(0L);

        when(rateLimitService.tryConsume(eq("user:john@example.com"), eq(Tier.AUTHENTICATED))).thenReturn(consumptionProbe);
        when(rateLimitService.getCapacityForTier(Tier.AUTHENTICATED)).thenReturn(100L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimitService).tryConsume("user:john@example.com", Tier.AUTHENTICATED);
        verify(filterChain).doFilter(request, response);
    }
}
