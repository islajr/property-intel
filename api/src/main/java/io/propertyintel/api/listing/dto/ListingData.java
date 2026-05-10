package io.propertyintel.api.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ListingData(

        @Schema(description = "Listing id", example = "40")
        Long id,

        @Schema(description = "Listing source", examples = {"propertypro", "privateproperty", "nigeriapropertycentre"})
        String source,

        @Schema(description = "Listing external URL", example = "https://propertypro.ng/2RL2E")
        String url,

        @Schema(description = "Listing title", example = "Prime property on Bourdillon Road, Ikoyi")
        String title,

        @Schema(description = "listing price (kobo)", example = "1000000")
        Long priceKobo,

        @Schema(description = "Listing price (naira)", example = "10000")
        String priceFormatted,

        @Schema(description = "Property type", examples = {"FLAT", "DETACHED_BUILDING", "SERVICE_APARTMENT"})
        String propertyType,

        @Schema(description = "Bedroom count", example = "3")
        Integer bedrooms,

        @Schema(description = "Bathroom count", example = "3")
        Integer bathrooms,

        @Schema(description = "Located neighbourhood", example = "Ikoyi")
        String neighbourhood,

        @Schema(description = "Located city", example = "LAGOS")
        String city,

        @Schema(description = "Listing latitudinal co-ordinates", example = "6.3525459")
        Double lat,

        @Schema(description = "Listing longitudinal co-ordinates", example = "4.520502")
        Double lng,

        @Schema(description = "Listing status", examples = {"ACTIVE", "REMOVED"})
        String listingStatus
) {}
