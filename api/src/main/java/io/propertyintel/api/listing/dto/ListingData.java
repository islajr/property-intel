package io.propertyintel.api.listing.dto;

public record ListingData(
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
        String listingStatus
) {}
