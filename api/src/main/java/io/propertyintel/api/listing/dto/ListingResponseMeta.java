package io.propertyintel.api.listing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record ListingResponseMeta(
        @Schema(description = "Total item count", example = "1000")
        int count,

        @Schema(description = "Next cursor hash", example = "eyJpZCI6MTIzNDV9")
        @JsonProperty("next_cursor")
        String nextCursor,

        @JsonProperty("has_more")
        @Schema(description = "Last page check", example = "false")
        boolean hasMore
) {}
