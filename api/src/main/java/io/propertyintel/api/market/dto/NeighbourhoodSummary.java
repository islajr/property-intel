package io.propertyintel.api.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NeighbourhoodSummary(
        @Schema(name = "Response Data")
        List<NeighbourhoodSummaryData> data,

        @Schema(name = "Response meta-data")
        NeighbourhoodSummaryMeta meta
) {}
