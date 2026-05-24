package io.propertyintel.api.global.ratelimit.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ProxyManager<String> proxyManager;

    @Value("${ratelimit.auth.capacity}")
    private long authCapacity;

    @Value("${ratelimit.auth.refill.duration}")
    private long authDuration;

    @Value("${ratelimit.public.capacity}")
    private long publicCapacity;

    @Value("${ratelimit.public.refill.duration}")
    private long publicDuration;

    @Value("${ratelimit.authenticated.capacity}")
    private long authenticatedCapacity;

    @Value("${ratelimit.authenticated.refill.duration}")
    private long authenticatedDuration;

    public enum Tier {
        AUTH, PUBLIC, AUTHENTICATED
    }

    public ConsumptionProbe tryConsume(String key, Tier tier) {
        return proxyManager.builder()
                .build(key, () -> getConfigurationForTier(tier))
                .tryConsumeAndReturnRemaining(1);
    }

    private BucketConfiguration getConfigurationForTier(Tier tier) {
        Bandwidth limit = switch (tier) {
            case AUTH -> Bandwidth.builder()
                    .capacity(authCapacity)
                    .refillGreedy(authCapacity, Duration.ofMinutes(authDuration))
                    .build();
            case PUBLIC -> Bandwidth.builder()
                    .capacity(publicCapacity)
                    .refillGreedy(publicCapacity, Duration.ofMinutes(publicDuration))
                    .build();
            case AUTHENTICATED -> Bandwidth.builder()
                    .capacity(authenticatedCapacity)
                    .refillGreedy(authenticatedCapacity, Duration.ofMinutes(authenticatedDuration))
                    .build();
        };

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }
    
    public long getCapacityForTier(Tier tier) {
        return switch (tier) {
            case AUTH -> authCapacity;
            case PUBLIC -> publicCapacity;
            case AUTHENTICATED -> authenticatedCapacity;
        };
    }
}
