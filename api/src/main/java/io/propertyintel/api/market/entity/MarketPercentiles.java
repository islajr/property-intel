package io.propertyintel.api.market.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public record MarketPercentiles(

        @Schema(description = "25th Percentile Values", example = "1203032")
        Long p25,

        @Schema(description = "Median Percentile Values", example = "42029913")
        Long p50,

        @Schema(description = "75th Percentile Values", example = "2002910039")
        Long p75,

        @Schema(description = "90th Percentile Values", example = "200199139")
        Long p90
) {
}
