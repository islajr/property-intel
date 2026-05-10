package io.propertyintel.api.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record PriceHistoryResponse(
        @Schema(description = "Returns an old price value (kobo) if any", example = "null")
        Long oldValue,

        @Schema(description = "Contains a new price value (kobo) if any", example = "1000000000")
        Long newValue,

        @Schema(description = "Specific listing event type", examples = {"LISTED", "PRICE_CHANGE", "REMOVED"})
        String eventType,

        @Schema(description = "Date of occurrence of the specific event", example = "2026-04-02")
        LocalDate eventDate
) {
}
