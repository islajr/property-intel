package io.propertyintel.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.propertyintel.api.auth.config.*;
import io.propertyintel.api.auth.service.UserDetailService;
import io.propertyintel.api.global.idempotency.config.IdempotencyInterceptor;
import io.propertyintel.api.global.ratelimit.config.RateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
public abstract class BaseControllerIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    protected JwtFilter jwtFilter;

    @MockitoBean
    protected RateLimitFilter rateLimitFilter;

    @MockitoBean
    protected UserDetailService userDetailService;

    @MockitoBean
    protected CustomAuthEntryPoint customAuthEntryPoint;

    @MockitoBean
    protected CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockitoBean
    protected JwtLogoutHandler jwtLogoutHandler;

    @MockitoBean
    protected JwtLogoutSuccessHandler jwtLogoutSuccessHandler;

    @MockitoBean
    protected IdempotencyInterceptor idempotencyInterceptor;

    @BeforeEach
    void setUpBaseFilters() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(rateLimitFilter).doFilter(any(), any(), any());

        when(idempotencyInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }
}
