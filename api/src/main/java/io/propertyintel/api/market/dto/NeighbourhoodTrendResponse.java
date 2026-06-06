package io.propertyintel.api.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NeighbourhoodTrendResponse(

        @Schema(description = "Neighbourhood name", example = "Ajah")
        String neighbourhood,

        @Schema(description = "Weekly stats trend for neighbourhood")
        List<NeighbourhoodTrendStats> weeks
) {
}
