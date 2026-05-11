package io.propertyintel.api.market;

import io.propertyintel.api.global.exception.exceptions.ResourceNotFoundException;
import io.propertyintel.api.global.util.RepositoryUtils;
import io.propertyintel.api.market.dto.NeighbourhoodStatsResponse;
import io.propertyintel.api.market.dto.NeighbourhoodSummary;
import io.propertyintel.api.market.mapper.MarketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketRepository marketRepository;
    private final RepositoryUtils repositoryUtils;
    private final MarketMapper marketMapper;

    public ResponseEntity<NeighbourhoodSummary> getNeighbourhoods(String sortBy, Integer limit, Integer page) {
        Sort sort = resolveSort(sortBy);
        Pageable pageable = repositoryUtils.buildPageable(limit, sort, page == null ? 0 : page);

        Page<Market> markets = marketRepository.findAll(pageable);

        if (markets.isEmpty()) throw new ResourceNotFoundException("No market neighbourhoods found");

        return(ResponseEntity.ok(marketMapper.toPaginatedResponse(markets)));
    }

    public ResponseEntity<NeighbourhoodStatsResponse> getNeighbourhoodStats(String neighbourhood) {
        Market market = marketRepository.findByNeighbourhood(neighbourhood).orElseThrow(
                () -> new ResourceNotFoundException("Neighbourhood not found"));

        return (ResponseEntity.ok(marketMapper.toStatsResponse(market)));


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
