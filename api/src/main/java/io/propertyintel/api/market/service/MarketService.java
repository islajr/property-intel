package io.propertyintel.api.market.service;

import io.propertyintel.api.global.caching.CacheNames;
import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import io.propertyintel.api.global.exception.exceptions.ResourceNotFoundException;
import io.propertyintel.api.market.dto.NeighbourhoodStatsResponse;
import io.propertyintel.api.market.dto.NeighbourhoodSummary;
import io.propertyintel.api.market.dto.NeighbourhoodSummaryData;
import io.propertyintel.api.market.dto.NeighbourhoodSummaryMeta;
import io.propertyintel.api.market.entity.Market;
import io.propertyintel.api.market.mapper.MarketMapper;
import io.propertyintel.api.market.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
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
public class MarketService {

    private final MarketRepository marketRepository;
    private final MarketMapper marketMapper;

    public NeighbourhoodSummary getNeighbourhoods(String sortBy, Integer limit, String cursor) {
        Sort sort = resolveSort(sortBy);

        Market lastMarket = null;
        if (cursor != null && !cursor.isBlank()) {
            String id = decodeMarket(cursor);
            if (id != null) lastMarket = marketRepository.findById(id).orElseThrow(
                    () -> new BadRequestException("Invalid cursor: market not found"));
        }


        Pageable pageable = PageRequest.of(0, limit + 1, sort);

        Page<Market> markets;

        if (lastMarket == null) {
            markets = marketRepository.findMarketSummary(pageable);
        } else {
            markets = marketRepository.findMarketSummaryWithCursor(lastMarket.getId(), pageable);
        }


        if (markets.isEmpty() && lastMarket == null) throw new ResourceNotFoundException("No current data for market neighbourhoods");

        boolean hasMore = markets.getSize() > limit;
        List<Market> content = markets.getContent();
        long count = markets.getTotalElements();
        List<Market> finalContent = hasMore ? content.subList(0, limit) : content;

        String nextCursor = "";
        if (!finalContent.isEmpty() && hasMore) {
            Market market = content.get(content.size() - 1);
            nextCursor = encodeMarket(market.getId());
        }


        NeighbourhoodSummaryMeta summaryMeta = NeighbourhoodSummaryMeta.builder()
                .count((int) count)
                .nextCursor(!nextCursor.isBlank() ? nextCursor : null)
                .hasMore(hasMore)
                .build();

        List<NeighbourhoodSummaryData> summaryData = finalContent.stream()
                .map(marketMapper::toNeighbourhoodSummary)
                .toList();


        return new NeighbourhoodSummary(summaryData, summaryMeta);
    }

    @Cacheable(value = CacheNames.MARKET_DETAILS, key = "#neighbourhood")
    public NeighbourhoodStatsResponse getNeighbourhoodStats(String neighbourhood) {
        Market market = marketRepository.findLatestRecordByNeighbourhood(neighbourhood).orElseThrow(
                () -> new ResourceNotFoundException("No current data for requested neighbourhood: %s".formatted(neighbourhood)));

        return (marketMapper.toStatsResponse(market));


    }

    public Sort resolveSort(String sort) {
        return switch (sort == null ? "neighbourhood" : sort) {
            case "neighbourhood" -> Sort.by(Sort.Direction.ASC, "neighbourhood");
            case "new_listings" -> Sort.by(Sort.Direction.DESC, "newListingsThisWeek");
            case "price_reduced" -> Sort.by(Sort.Direction.DESC, "priceReducedThisWeek");
            case "median_price" -> Sort.by(Sort.Direction.DESC, "medianPriceKobo");
            case "active_listings" -> Sort.by(Sort.Direction.DESC, "activeListingCount");
            default -> throw new IllegalArgumentException(
                    "Invalid sort option: %s. Valid options: neighbourhood, new_listings, price_reduced, median_price, active_listings".formatted(sort)
            );  //
        };
    }


}
