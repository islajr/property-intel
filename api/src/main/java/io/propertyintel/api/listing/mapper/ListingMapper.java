package io.propertyintel.api.listing.mapper;

import io.propertyintel.api.listing.entity.Listing;
import io.propertyintel.api.listing.entity.PriceHistory;
import io.propertyintel.api.listing.dto.*;
import io.propertyintel.api.global.util.CurrencyUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", imports = CurrencyUtils.class)
public interface ListingMapper {

    @Mapping(source = "content", target = "data")
    @Mapping(source = ".", target = "meta")
    ListingResponse toResponse(Page<Listing> listings);

    @Mapping(target = "priceFormatted",
            expression = "java(CurrencyUtils.formatKoboToNaira(listing.getPriceKobo()))")
    ListingData toListingData(Listing listing);

    @Mapping(source = "totalElements", target = "count")
    @Mapping(source = "number", target = "pageNumber")
    @Mapping(source = "totalPages", target = "pageCount")
    @Mapping(source = "size", target = "pageSize")
    @Mapping(source = "last", target = "isLast")
    ListingResponseMeta toMeta(Page<?> page);

    @Mapping(source = "priceHistory", target = "listingHistory")
    @Mapping(target = "priceFormatted",
            expression = "java(CurrencyUtils.formatKoboToNaira(listing.getPriceKobo()))")
    ListingDetailResponse toDetailResponse(Listing listing);

    PriceHistoryResponse toPriceHistoryResponse(PriceHistory priceHistory);
}
