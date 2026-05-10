package io.propertyintel.api.search;

import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<ListingResponse> basicSearch(ListingSearchParams params, BindingResult bindingResult) {
        if (bindingResult != null && bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return searchService.performSearch(params);
    }
}