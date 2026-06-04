package io.propertyintel.api.search.service;

import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.listing.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ListingService listingService;

    public ListingResponse performSearch(ListingSearchParams params) {
        log.info("Performing listing search via SearchService. Query Params: neighbourhood={}, type={}, minPrice={}, maxPrice={}",
                params.getNeighbourhood(), params.getType(), params.getMin_price(), params.getMax_price());
        
        ListingResponse response = listingService.getListings(params);
        log.debug("Delegated listing search completed in SearchService. Found: {} items", response.data().size());
        return response;
    }
}
