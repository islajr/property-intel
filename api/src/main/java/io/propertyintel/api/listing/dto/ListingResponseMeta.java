package io.propertyintel.api.listing.dto;

public record ListingResponseMeta(

        int count,
        int pageNumber,
        int pageCount,
        int pageSize,
        boolean isLast
) {}
