package io.propertyintel.api.market.dto;

import io.propertyintel.api.market.MarketPercentiles;
import io.swagger.v3.oas.annotations.media.Schema;

public record NeighbourhoodStatsResponse(

        @Schema(description = "Neighbourhood name", example = "Gbagada")
        String neighbourhood,

        @Schema(description = "City name", example = "LAGOS")
        String city,

        @Schema(description = "Median price of listings in area (kobo)", example = "1452999223")
        Long medianPriceKobo,

        @Schema(description = "Median price of listings in area (naira)", example = "14529992.23")
        String formattedMedianPrice,

        @Schema(description = "Active listing count within neighbourhood", example = "25")
        Integer activeListingCount,

        @Schema(description = "New listings in current week", example = "10")
        Integer newListingsThisWeek,

        @Schema(description = "Listings with reduced prices this week", example = "54")
        Integer priceReducedThisWeek,

        @Schema(description = "Market percentile values")
        MarketPercentiles pricePercentiles
) {
}
