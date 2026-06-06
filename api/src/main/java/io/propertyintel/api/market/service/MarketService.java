package io.propertyintel.api.market.service;

import io.propertyintel.api.global.caching.CacheNames;
import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import io.propertyintel.api.global.exception.exceptions.ResourceNotFoundException;
import io.propertyintel.api.market.dto.*;
import io.propertyintel.api.market.entity.Market;
import io.propertyintel.api.market.mapper.MarketMapper;
import io.propertyintel.api.market.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.propertyintel.api.global.util.CursorPagination.decodeMarket;
import static io.propertyintel.api.global.util.CursorPagination.encodeMarket;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

    private final MarketRepository marketRepository;
    private final MarketMapper marketMapper;

    @Cacheable(
            value = CacheNames.MARKET_NEIGHBOURHOODS,
            key = "(#sortBy != null ? #sortBy : 'neighbourhood') + '_' + (#limit != null ? #limit : 20) + '_' + (#cursor != null ? #cursor : '')"
    )
    public NeighbourhoodSummary getNeighbourhoods(String sortBy, Integer limit, String cursor) {
        log.info("Fetching neighbourhood summary. Sort by: {}, limit: {}, cursor: {}", sortBy, limit, cursor);
        Sort sort = resolveSort(sortBy);

        Market lastMarket = null;
        if (cursor != null && !cursor.isBlank()) {
            log.debug("Decoding pagination cursor: {}", cursor);
            String id = decodeMarket(cursor);
            if (id != null) {
                lastMarket = marketRepository.findById(id).orElseThrow(() -> {
                    log.warn("Invalid cursor provided. Market not found for ID: {}", id);
                    return new BadRequestException("Invalid cursor: market not found");
                });
            }
        }

        Pageable pageable = PageRequest.of(0, limit + 1, sort);

        Page<Market> markets;

        if (lastMarket == null) {
            log.debug("Fetching market summary without cursor (first page). Limit: {}", limit);
            markets = marketRepository.findMarketSummary(pageable);
        } else {
            log.debug("Fetching market summary with cursor. Last ID: {}, Limit: {}", lastMarket.getId(), limit);
            markets = marketRepository.findMarketSummaryWithCursor(lastMarket.getId(), pageable);
        }

        if (markets.isEmpty() && lastMarket == null) {
            log.warn("No neighbourhood snapshots found in the repository.");
            throw new ResourceNotFoundException("No current data for market neighbourhoods");
        }

        boolean hasMore = markets.getSize() > limit;
        List<Market> content = markets.getContent();
        long count = markets.getTotalElements();
        List<Market> finalContent = hasMore ? content.subList(0, limit) : content;

        String nextCursor = "";
        if (!finalContent.isEmpty() && hasMore) {
            Market market = content.get(content.size() - 1);
            nextCursor = encodeMarket(market.getId());
            log.debug("Generated next pagination cursor: {}", nextCursor);
        }

        NeighbourhoodSummaryMeta summaryMeta = NeighbourhoodSummaryMeta.builder()
                .count((int) count)
                .nextCursor(!nextCursor.isBlank() ? nextCursor : null)
                .hasMore(hasMore)
                .build();

        List<NeighbourhoodSummaryData> summaryData = finalContent.stream()
                .map(marketMapper::toNeighbourhoodSummary)
                .toList();

        log.info("Successfully fetched {} neighbourhood summary records.", summaryData.size());
        return new NeighbourhoodSummary(summaryData, summaryMeta);
    }

    @Cacheable(value = CacheNames.MARKET_DETAILS, key = "#neighbourhood")
    public NeighbourhoodStatsResponse getNeighbourhoodStats(String neighbourhood) {
        log.info("Fetching stats for neighbourhood: {}", neighbourhood);
        Market market = marketRepository.findLatestRecordByNeighbourhood(neighbourhood).orElseThrow(() -> {
            log.warn("No market data found for neighbourhood: {}", neighbourhood);
            return new ResourceNotFoundException("No current data for requested neighbourhood: %s".formatted(neighbourhood));
        });

        NeighbourhoodStatsResponse statsResponse = marketMapper.toStatsResponse(market);
        log.info("Successfully retrieved stats for neighbourhood: {}", neighbourhood);
        return statsResponse;
    }

    @Cacheable(value = CacheNames.MARKET_TRENDS, key = "#neighbourhood")
    public NeighbourhoodTrendResponse getNeighbourhoodTrends(String neighbourhood) {
        log.info("Fetching trends for neighbourhood: {}", neighbourhood);
        List<Market> marketTrends = marketRepository.findMarketTrends(neighbourhood);

        if (marketTrends.isEmpty()) {
            log.warn("No trend data found for neighbourhood: {}", neighbourhood);
            throw new ResourceNotFoundException("No trend data for requested neighbourhood: %s".formatted(neighbourhood));
        }

        List<NeighbourhoodTrendStats> trendStats = marketTrends.stream()
                .map(marketMapper::toNeighbourhoodTrendStats)
                .toList();

        log.info("Successfully retrieved trend stats for neighbourhood: {}", neighbourhood);
        return new NeighbourhoodTrendResponse(neighbourhood, trendStats);
    }

    public Sort resolveSort(String sort) {
        log.debug("Resolving sort criteria for: {}", sort);
        return switch (sort == null ? "neighbourhood" : sort) {
            case "neighbourhood" -> Sort.by(Sort.Direction.ASC, "neighbourhood");
            case "new_listings" -> Sort.by(Sort.Direction.DESC, "newListingsThisWeek");
            case "price_reduced" -> Sort.by(Sort.Direction.DESC, "priceReducedThisWeek");
            case "median_price" -> Sort.by(Sort.Direction.DESC, "medianPriceKobo");
            case "active_listings" -> Sort.by(Sort.Direction.DESC, "activeListingCount");
            default -> {
                log.warn("Invalid sorting option provided: {}", sort);
                throw new IllegalArgumentException(
                        "Invalid sort option: %s. Valid options: neighbourhood, new_listings, price_reduced, median_price, active_listings".formatted(sort)
                );
            }
        };
    }
}
