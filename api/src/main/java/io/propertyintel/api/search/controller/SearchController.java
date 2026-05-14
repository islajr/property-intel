package io.propertyintel.api.search.controller;

import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Search Feature", description = "Provides search feature for the API")
public class SearchController {
    private final SearchService searchService;

    @Operation(description = "Returns listing data based on provided search filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listing info successfully retrieved")
    })
    @GetMapping("/api/v1/search")
    public ResponseEntity<ListingResponse> basicSearch(@Valid ListingSearchParams params) {
        return searchService.performSearch(params);
    }
}