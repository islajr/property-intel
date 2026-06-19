package io.propertyintel.api.global.ratelimit.service;

import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import io.propertyintel.api.global.ratelimit.service.RateLimitService.Tier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private ProxyManager<String> proxyManager;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rateLimitService, "authCapacity", 10L);
        ReflectionTestUtils.setField(rateLimitService, "authDuration", 1L);
        ReflectionTestUtils.setField(rateLimitService, "publicCapacity", 20L);
        ReflectionTestUtils.setField(rateLimitService, "publicDuration", 2L);
        ReflectionTestUtils.setField(rateLimitService, "authenticatedCapacity", 30L);
        ReflectionTestUtils.setField(rateLimitService, "authenticatedDuration", 3L);
    }

    @Test
    void testGetCapacityForTier() {
        assertEquals(10L, rateLimitService.getCapacityForTier(Tier.AUTH));
        assertEquals(20L, rateLimitService.getCapacityForTier(Tier.PUBLIC));
        assertEquals(30L, rateLimitService.getCapacityForTier(Tier.AUTHENTICATED));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testTryConsumeInvokesProxyManager() {
        RemoteBucketBuilder<String> builder = mock(RemoteBucketBuilder.class);
        BucketProxy bucket = mock(BucketProxy.class);
        ConsumptionProbe probe = mock(ConsumptionProbe.class);

        when(proxyManager.builder()).thenReturn(builder);
        when(builder.build(eq("test-key"), any(Supplier.class))).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        ConsumptionProbe result = rateLimitService.tryConsume("test-key", Tier.PUBLIC);

        assertNotNull(result);
        assertEquals(probe, result);

        verify(proxyManager).builder();
        verify(builder).build(eq("test-key"), any(Supplier.class));
        verify(bucket).tryConsumeAndReturnRemaining(1);
    }
}
