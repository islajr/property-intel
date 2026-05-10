package io.propertyintel.api.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NeighbourhoodSummaryMeta(
        @Schema(description = "Total item count", example = "1000")
        int count,

        @Schema(description = "Current page number", example = "23")
        int pageNumber,

        @Schema(description = "Total number of available pages of data", example = "50")
        int pageCount,

        @Schema(description = "Number of items per page", example = "20")
        int pageSize,

        @Schema(description = "Last page check", example = "false")
        boolean isLast
) {}
