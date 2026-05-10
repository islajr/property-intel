package io.propertyintel.api.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SnapshotScheduler {

    private final MarketRepository marketRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    private void refreshMarketSnapshot() {
        log.info("Starting market snapshot refresh...");

        long startTime = System.currentTimeMillis();
        marketRepository.refreshMarketSnapshot();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Market snapshot refresh completed in {} ms", duration);
    }
}
