package io.propertyintel.api.global.config;

import io.propertyintel.api.global.util.CacheNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        log.info("Initializing default Redis cache configuration");
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.
                        fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(RedisSerializer.json())
                )
                        .disableCachingNullValues()
                        .entryTtl(Duration.ofHours(1));


        Map<String, RedisCacheConfiguration> cacheConfigurationMap = new HashMap<>();

        log.info("Adding specialized cache configurations");
        cacheConfigurationMap.put(CacheNames.MARKET_DETAILS, cacheConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigurationMap.put(CacheNames.LISTING_DETAILS, cacheConfig.entryTtl(Duration.ofHours(6)));

        log.info("Successfully configured Redis cache manager");

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .withInitialCacheConfigurations(cacheConfigurationMap)
                .transactionAware().build();


    }
}
