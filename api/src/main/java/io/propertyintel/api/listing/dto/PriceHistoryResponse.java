package io.propertyintel.api.listing.dto;

import java.time.LocalDate;

public record PriceHistoryResponse(
        Long oldValue,
        Long newValue,
        String eventType,
        LocalDate eventDate
) {
}
