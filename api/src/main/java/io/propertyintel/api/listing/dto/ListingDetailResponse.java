package io.propertyintel.api.listing.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ListingDetailResponse(
        Long id,
        String source,
        String url,
        String title,
        Long priceKobo,
        Long priceFormatted,
        String propertyType,
        Integer bedrooms,
        Integer bathrooms,
        String neighbourhood,
        String city,
        Double lat,
        Double lng,
        String listingStatus,
        List<PriceHistoryResponse> listingHistory

) {}