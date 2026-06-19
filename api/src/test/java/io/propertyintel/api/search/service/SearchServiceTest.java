package io.propertyintel.api.search.service;

import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.listing.service.ListingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private ListingService listingService;

    @InjectMocks
    private SearchService searchService;

    @Test
    void testPerformSearch() {
        ListingSearchParams params = new ListingSearchParams();
        ListingResponse mockResponse = new ListingResponse(java.util.Collections.emptyList(), null);

        when(listingService.getListings(params)).thenReturn(mockResponse);

        ListingResponse actualResponse = searchService.performSearch(params);

        assertNotNull(actualResponse);
        assertSame(mockResponse, actualResponse);

        verify(listingService).getListings(params);
    }
}
