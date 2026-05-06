package io.propertyintel.api.listing.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ListingResponse(
        List<ListingData> data,
        ListingResponseMeta meta
) {
}