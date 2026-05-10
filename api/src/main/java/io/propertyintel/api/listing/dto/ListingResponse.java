package io.propertyintel.api.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record ListingResponse(
        @Schema(description = "Response data")
        List<ListingData> data,

        @Schema(description = "Response metadata")
        ListingResponseMeta meta
) {
}