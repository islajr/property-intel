package io.propertyintel.api.market.service;

import io.propertyintel.api.global.caching.CacheNames;
import io.propertyintel.api.market.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SnapshotScheduler {

    private final MarketRepository marketRepository;

    @Scheduled(cron = "0 0 2 * * MON", zone = "GMT+1")    // Every monday at 02:00 GMT + 1
    @Caching(evict = {
            @CacheEvict(value = CacheNames.MARKET_DETAILS, allEntries = true),
            @CacheEvict(value = CacheNames.MARKET_TRENDS, allEntries = true),
            @CacheEvict(value = CacheNames.MARKET_NEIGHBOURHOODS, allEntries = true)
    })
    public void refreshMarketSnapshot() {
        log.info("-----Starting market snapshot refresh-----");

        long startTime = System.currentTimeMillis();

        try {
            marketRepository.refreshMarketSnapshot();
            long duration = System.currentTimeMillis() - startTime;
            log.info("Market snapshot refresh completed in {} ms", duration);
        } catch (InvalidDataAccessResourceUsageException ex) {
            log.error("Market snapshot refresh failed", ex);
        }

    }
}
