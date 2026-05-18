package io.propertyintel.api.search.service;

import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.listing.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ListingService listingService;

    public ListingResponse performSearch(ListingSearchParams params) {
        /* Delegates to listing search for now as spec details similar features */
        return listingService.getListings(params);
    }
}
