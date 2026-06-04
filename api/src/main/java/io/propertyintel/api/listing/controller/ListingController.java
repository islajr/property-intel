package io.propertyintel.api.listing.controller;

import io.propertyintel.api.global.exception.ErrorResponse;
import io.propertyintel.api.listing.dto.ListingDetailResponse;
import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.listing.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Listing Operations", description = "Provides all data pertaining to existing property listings")
public class ListingController {

    private final ListingService listingService;

    @Operation(summary = "Find all listings that fit a certain criteria",
            description = "Returns a paginated array of listings that satisfy the provided search and filter criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings successfully retrieved",
                    content = @Content(schema = @Schema(implementation = ListingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters or pagination cursor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No listings found matching the criteria",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/listings")
    public ResponseEntity<ListingResponse> getListings(@Valid ListingSearchParams searchParams) {
        return ResponseEntity.ok(listingService.getListings(searchParams));
    }

    @Operation(summary = "Find information on a specific listings",
            description = "Returns deeper information on requested listing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listing info successfully retrieved",
                    content = @Content(schema = @Schema(implementation = ListingDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Listing with the specified id not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/listings/{id}")
    public ResponseEntity<ListingDetailResponse> getListing(
            @Parameter(description = "Listing id", example = "23", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(listingService.getListing(id));
    }
}