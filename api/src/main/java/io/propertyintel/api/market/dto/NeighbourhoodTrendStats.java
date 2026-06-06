package io.propertyintel.api.market.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record NeighbourhoodTrendStats(

        @Schema(description = "Start of the week", example = "2016-06-02")
        @JsonProperty("week_start")
        LocalDate weekStart,

        @Schema(description = "Neighbourhood median price (kobo)", example = "1000000000000")
        @JsonProperty("median_price_kobo")
        Long medianPriceKobo,

        @Schema(description = "Weekly neighbourhood active listing count", example = "25")
        @JsonProperty("active_listings")
        Integer activeListings,

        @Schema(description = "Weekly neighbourhood new listings", example = "5")
        @JsonProperty("new_listings")
        Integer newListings
) {
}
