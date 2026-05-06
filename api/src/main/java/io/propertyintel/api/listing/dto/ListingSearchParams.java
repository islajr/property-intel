package io.propertyintel.api.listing.dto;

import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListingSearchParams {

    String neighbourhood;
    String type;
    Integer min_beds;
    Integer max_beds;
    Long min_price;
    Long max_price;
    Integer max_days;
    Boolean price_reduced = false;
    String sort = "newest";
    Integer page = 0;

    @Max(value = 50, message = "limit cannot exceed 50")
    Integer limit = 20;
    Boolean include_inactive = false;
}