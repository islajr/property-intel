package io.propertyintel.api.market.service;

import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import io.propertyintel.api.global.exception.exceptions.ResourceNotFoundException;
import io.propertyintel.api.global.util.CursorPagination;
import io.propertyintel.api.market.dto.NeighbourhoodStatsResponse;
import io.propertyintel.api.market.dto.NeighbourhoodSummary;
import io.propertyintel.api.market.dto.NeighbourhoodSummaryData;
import io.propertyintel.api.market.dto.NeighbourhoodTrendResponse;
import io.propertyintel.api.market.dto.NeighbourhoodTrendStats;
import io.propertyintel.api.market.entity.Market;
import io.propertyintel.api.market.entity.MarketPercentiles;
import io.propertyintel.api.market.mapper.MarketMapper;
import io.propertyintel.api.market.repository.MarketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

    @Mock
    private MarketRepository marketRepository;

    @Mock
    private MarketMapper marketMapper;

    @InjectMocks
    private MarketService marketService;

    private Market sampleMarket1;
    private Market sampleMarket2;

    @BeforeEach
    void setUp() {
        sampleMarket1 = new Market();
        sampleMarket1.setId("market-1");
        sampleMarket1.setNeighbourhood("Ajah");
        sampleMarket1.setCity("Lagos");
        sampleMarket1.setMedianPriceKobo(15000000.0);

        sampleMarket2 = new Market();
        sampleMarket2.setId("market-2");
        sampleMarket2.setNeighbourhood("Ikoyi");
        sampleMarket2.setCity("Lagos");
        sampleMarket2.setMedianPriceKobo(50000000.0);
    }

    @Test
    void testGetNeighbourhoodStatsSuccess() {
        when(marketRepository.findLatestRecordByNeighbourhood("Ajah")).thenReturn(Optional.of(sampleMarket1));
        
        NeighbourhoodStatsResponse expectedResponse = new NeighbourhoodStatsResponse(
                "Ajah", "Lagos", 15000000L, "150,000", 10, 5.5, 3, 2,
                new MarketPercentiles(10000000L, 15000000L, 20000000L, 25000000L)
        );
        when(marketMapper.toStatsResponse(sampleMarket1)).thenReturn(expectedResponse);

        NeighbourhoodStatsResponse actualResponse = marketService.getNeighbourhoodStats("Ajah");

        assertNotNull(actualResponse);
        assertEquals("Ajah", actualResponse.neighbourhood());
        assertEquals(15000000L, actualResponse.medianPriceKobo());
        verify(marketRepository).findLatestRecordByNeighbourhood("Ajah");
        verify(marketMapper).toStatsResponse(sampleMarket1);
    }

    @Test
    void testGetNeighbourhoodStatsNotFound() {
        when(marketRepository.findLatestRecordByNeighbourhood("Unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> marketService.getNeighbourhoodStats("Unknown"));
        verify(marketRepository).findLatestRecordByNeighbourhood("Unknown");
    }

    @Test
    void testGetNeighbourhoodsWithoutCursor() {
        Page<Market> page = new PageImpl<>(List.of(sampleMarket1, sampleMarket2));
        when(marketRepository.findMarketSummary(any(Pageable.class))).thenReturn(page);

        NeighbourhoodSummaryData data1 = new NeighbourhoodSummaryData("Ajah", "Lagos", 10, 15000000L, "150,000");
        NeighbourhoodSummaryData data2 = new NeighbourhoodSummaryData("Ikoyi", "Lagos", 5, 50000000L, "500,000");
        when(marketMapper.toNeighbourhoodSummary(sampleMarket1)).thenReturn(data1);
        when(marketMapper.toNeighbourhoodSummary(sampleMarket2)).thenReturn(data2);

        NeighbourhoodSummary summary = marketService.getNeighbourhoods("neighbourhood", 2, null);

        assertNotNull(summary);
        assertEquals(2, summary.data().size());
        assertEquals("Ajah", summary.data().get(0).neighbourhood());
        assertFalse(summary.meta().hasMore());
        assertNull(summary.meta().nextCursor());

        verify(marketRepository).findMarketSummary(any(Pageable.class));
    }

    @Test
    void testGetNeighbourhoodsWithCursor() {
        String cursor = CursorPagination.encodeMarket("market-1");
        when(marketRepository.findById("market-1")).thenReturn(Optional.of(sampleMarket1));

        Page<Market> page = new PageImpl<>(List.of(sampleMarket1, sampleMarket2)); // size is 2, limit is 1 -> hasMore is true
        when(marketRepository.findMarketSummaryWithCursor(eq("market-1"), any(Pageable.class))).thenReturn(page);

        NeighbourhoodSummaryData data1 = new NeighbourhoodSummaryData("Ajah", "Lagos", 10, 15000000L, "150,000");
        when(marketMapper.toNeighbourhoodSummary(sampleMarket1)).thenReturn(data1);

        NeighbourhoodSummary summary = marketService.getNeighbourhoods("neighbourhood", 1, cursor);

        assertNotNull(summary);
        assertEquals(1, summary.data().size());
        assertEquals("Ajah", summary.data().get(0).neighbourhood());
        assertTrue(summary.meta().hasMore());
        assertEquals(CursorPagination.encodeMarket("market-2"), summary.meta().nextCursor());

        verify(marketRepository).findById("market-1");
        verify(marketRepository).findMarketSummaryWithCursor(eq("market-1"), any(Pageable.class));
    }

    @Test
    void testGetNeighbourhoodsInvalidCursor() {
        String cursor = CursorPagination.encodeMarket("invalid-id");
        when(marketRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> marketService.getNeighbourhoods("neighbourhood", 10, cursor));
        verify(marketRepository).findById("invalid-id");
    }

    @Test
    void testGetNeighbourhoodsEmptyAndNoCursor() {
        when(marketRepository.findMarketSummary(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        assertThrows(ResourceNotFoundException.class, () -> marketService.getNeighbourhoods("neighbourhood", 10, null));
    }

    @Test
    void testResolveSortOptions() {
        assertEquals(Sort.by(Sort.Direction.ASC, "neighbourhood"), marketService.resolveSort("neighbourhood"));
        assertEquals(Sort.by(Sort.Direction.DESC, "new_listings_count"), marketService.resolveSort("new_listings"));
        assertEquals(Sort.by(Sort.Direction.DESC, "price_reduced_count"), marketService.resolveSort("price_reduced"));
        assertEquals(Sort.by(Sort.Direction.DESC, "median_price_kobo"), marketService.resolveSort("median_price"));
        assertEquals(Sort.by(Sort.Direction.DESC, "active_listing_count"), marketService.resolveSort("active_listings"));
        assertThrows(IllegalArgumentException.class, () -> marketService.resolveSort("invalid_sort"));
    }

    @Test
    void testGetNeighbourhoodTrendsSuccess() {
        when(marketRepository.findMarketTrends("Ajah")).thenReturn(List.of(sampleMarket1));

        NeighbourhoodTrendStats trendStats = new NeighbourhoodTrendStats(
                java.time.LocalDate.now(), 15000000L, 10, 5
        );
        when(marketMapper.toNeighbourhoodTrendStats(sampleMarket1)).thenReturn(trendStats);

        NeighbourhoodTrendResponse response = marketService.getNeighbourhoodTrends("Ajah");

        assertNotNull(response);
        assertEquals("Ajah", response.neighbourhood());
        assertEquals(1, response.weeks().size());
        assertEquals(15000000L, response.weeks().get(0).medianPriceKobo());

        verify(marketRepository).findMarketTrends("Ajah");
        verify(marketMapper).toNeighbourhoodTrendStats(sampleMarket1);
    }

    @Test
    void testGetNeighbourhoodTrendsNotFound() {
        when(marketRepository.findMarketTrends("Unknown")).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> marketService.getNeighbourhoodTrends("Unknown"));

        verify(marketRepository).findMarketTrends("Unknown");
    }
}
