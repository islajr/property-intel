package io.propertyintel.api.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListingSearchParams {

    @Schema(description = "Required neighbourhood", example = "Ikorodu")
    String neighbourhood;

    @Schema(description = "Required property type", example = "FLAT_APARTMENT")
    String type;

    @Schema(description = "Minimum number of bedrooms", example = "1")
    @Min(value = 0, message = "Bedroom count cannot be fewer than 0")
    Integer min_beds;

    @Schema(description = "Maximum number of bedrooms", example = "5")
    @Min(value = 0, message = "Bedroom count cannot be fewer than 0")
    Integer max_beds;

    @Schema(description = "Minimum price (kobo)", example = "1000000")
    @Min(value = 0, message = "Price cannot be less than than 0 NGN")
    Long min_price;

    @Schema(description = "Maximum price (kobo)", example = "50000000")
    @Min(value = 0, message = "Price cannot be less than 0 NGN")
    Long max_price;

    @Schema(description = "Maximum number of days on market", example = "5")
    @Min(value = 0, message = "Max days cannot be less than 0")
    Integer max_days;

    @Schema(description = "Price reduction check", example = "true")
    Boolean price_reduced = false;

    @Schema(description = "Sorting technique", example = "newest")
    String sort = "newest";

    @Schema(description = "Pagination cursor", example = "eyJpZCB6NYzM30=")
    String cursor;

    @Schema(description = "Maximum number of information per page", example = "20")
    @Min(value = 1, message = "Cannot request fewer than 1 item per page")
    @Max(value = 50, message = "Limit cannot exceed 50")
    Integer limit = 20;

    @Schema(description = "Include both active and removed listings", example = "false")
    Boolean include_inactive = false;
}