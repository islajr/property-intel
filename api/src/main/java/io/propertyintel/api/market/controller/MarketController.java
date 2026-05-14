package io.propertyintel.api.market.controller;

import io.propertyintel.api.market.service.MarketService;
import io.propertyintel.api.market.dto.NeighbourhoodStatsResponse;
import io.propertyintel.api.market.dto.NeighbourhoodSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Market Operations", description = "Provides all data pertaining to markets and specific neighbourhoods")
public class MarketController {
    private final MarketService marketService;

    @Operation(summary = "Find information on all neighbourhood markets",
            description = "Returns data on all neighbourhood markets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Neighbourhoods successfully retrieved")
    })
    @GetMapping("/api/v1/market/neighbourhoods")
    public ResponseEntity<NeighbourhoodSummary> getNeighbourhoods(
            @Parameter(description = "Sorting technique (neighbourhood | new_listings | price_reduced | median_price | active_listings)",
            example = "neighbourhood")
            @RequestParam(required = false) String sort_by,

            @Parameter(description = "Items per page (minimum = 0, maximum = 50)", example = "10")
            @Min(value = 1, message = "Cannot request fewer than 1 item per page")
            @Max(value = 50, message = "Cannot request more than 50 items per page")
            @RequestParam(required = false) Integer limit,

            @Parameter(description = "Page number offset (example = 5)", example = "5")
            @Min(value = 0, message = "Page number cannot be negative")
            @RequestParam(required = false) Integer page
            ) {
        return marketService.getNeighbourhoods(sort_by, limit, page);
    }

    @Operation(summary = "Find information on specific neighbourhood markets",
            description = "Returns deeper information on requested neighbourhood market")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Neighbourhood data successfully retrieved")
    })
    @GetMapping("/api/v1/market/{neighbourhood}/stats")
    public ResponseEntity<NeighbourhoodStatsResponse> getNeighbourhoodStats(
            @Parameter(description = "Neighbourhood", example = "Ajah", required = true)
                @PathVariable String neighbourhood) {
        return marketService.getNeighbourhoodStats(neighbourhood);
    }


}