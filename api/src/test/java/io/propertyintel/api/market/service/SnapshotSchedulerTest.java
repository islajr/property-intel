package io.propertyintel.api.market.service;

import io.propertyintel.api.market.repository.MarketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnapshotSchedulerTest {

    @Mock
    private MarketRepository marketRepository;

    @InjectMocks
    private SnapshotScheduler snapshotScheduler;

    @Test
    void testRefreshMarketSnapshotSuccess() {
        doNothing().when(marketRepository).refreshMarketSnapshot();

        snapshotScheduler.refreshMarketSnapshot();

        verify(marketRepository, times(1)).refreshMarketSnapshot();
    }

    @Test
    void testRefreshMarketSnapshotFailureGracefullyCaught() {
        doThrow(new InvalidDataAccessResourceUsageException("Database snapshot error"))
                .when(marketRepository).refreshMarketSnapshot();

        // This should not throw any exception as it is caught inside a try-catch block
        snapshotScheduler.refreshMarketSnapshot();

        verify(marketRepository, times(1)).refreshMarketSnapshot();
    }
}
