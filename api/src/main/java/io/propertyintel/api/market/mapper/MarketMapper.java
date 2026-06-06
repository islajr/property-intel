package io.propertyintel.api.market.mapper;

import io.propertyintel.api.global.util.CurrencyUtils;
import io.propertyintel.api.market.dto.NeighbourhoodStatsResponse;
import io.propertyintel.api.market.dto.NeighbourhoodSummaryData;
import io.propertyintel.api.market.dto.NeighbourhoodTrendStats;
import io.propertyintel.api.market.entity.Market;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = CurrencyUtils.class)
public interface MarketMapper {

    @Mapping(target = "formattedMedianPrice",
            expression = "java(CurrencyUtils.formatDoubleKoboToNaira(market.getMedianPriceKobo()))")
    NeighbourhoodSummaryData toNeighbourhoodSummary(Market market);

    @Mapping(target = "formattedMedianPrice",
            expression = "java(CurrencyUtils.formatDoubleKoboToNaira(market.getMedianPriceKobo()))")
    @Mapping(target = "pricePercentiles.p25", source = "p25")
    @Mapping(target = "pricePercentiles.p50", source = "medianPriceKobo")
    @Mapping(target = "pricePercentiles.p75", source = "p75")
    @Mapping(target = "pricePercentiles.p90", source = "p90")
    NeighbourhoodStatsResponse toStatsResponse(Market market);

    @Mapping(target = "weekStart", source = "snapshotWeek")
    @Mapping(target = "activeListings", source = "activeListingCount")
    @Mapping(target = "newListings", source = "newListingsCount")
    NeighbourhoodTrendStats toNeighbourhoodTrendStats(Market market);
}
