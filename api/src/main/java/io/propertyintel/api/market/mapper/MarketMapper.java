package io.propertyintel.api.market.mapper;

import io.propertyintel.api.market.entity.Market;
import io.propertyintel.api.market.entity.MarketPercentiles;
import io.propertyintel.api.market.dto.NeighbourhoodStatsResponse;
import io.propertyintel.api.market.dto.NeighbourhoodSummaryData;
import io.propertyintel.api.market.dto.NeighbourhoodSummaryMeta;
import io.propertyintel.api.global.util.CurrencyUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", imports = CurrencyUtils.class)
public interface MarketMapper {

    @Mapping(target = "formattedMedianPrice",
            expression = "java(CurrencyUtils.formatDoubleKoboToNaira(market.getMedianPriceKobo()))")
    NeighbourhoodSummaryData toNeighbourhoodSummary(Market market);

    @Mapping(source = "totalElements", target = "count")
    @Mapping(source = "last", target = "hasMore")
    NeighbourhoodSummaryMeta toNeighbourhoodSummaryMeta(Page<?> page);

    @Mapping(target = "formattedMedianPrice",
            expression = "java(CurrencyUtils.formatDoubleKoboToNaira(market.getMedianPriceKobo()))")
    @Mapping(target = "pricePercentiles.p25", source = "p25")
    @Mapping(target = "pricePercentiles.p50", source = "medianPriceKobo")
    @Mapping(target = "pricePercentiles.p75", source = "p75")
    @Mapping(target = "pricePercentiles.p90", source = "p90")
    NeighbourhoodStatsResponse toStatsResponse(Market market);

    @Mapping(source = "p25", target = "p25")
    @Mapping(source = "medianPriceKobo", target = "p50")
    @Mapping(source = "p75", target = "p75")
    @Mapping(source = "p90", target = "p90")
    MarketPercentiles marketPercentiles(Market market);
}
