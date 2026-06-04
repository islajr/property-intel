package io.propertyintel.api.listing.mapper;

import io.propertyintel.api.listing.entity.Listing;
import io.propertyintel.api.listing.entity.PriceHistory;
import io.propertyintel.api.listing.dto.*;
import io.propertyintel.api.global.util.CurrencyUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = CurrencyUtils.class)
public interface ListingMapper {

    @Mapping(target = "priceFormatted",
            expression = "java(CurrencyUtils.formatKoboToNaira(listing.getPriceKobo()))")
    ListingData toListingData(Listing listing);

    @Mapping(source = "priceHistory", target = "listingHistory")
    @Mapping(target = "priceFormatted",
            expression = "java(CurrencyUtils.formatKoboToNaira(listing.getPriceKobo()))")
    ListingDetailResponse toDetailResponse(Listing listing);

    PriceHistoryResponse toPriceHistoryResponse(PriceHistory priceHistory);
}
