package io.propertyintel.api.market;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(schema = "raw_data", name = "market_snapshot_view")
@Getter
@Setter
public class Market {

    @Id
    private String id;
    private String neighbourhood;
    private String city;
    private Double medianPriceKobo;
    private Integer activeListingCount;
    private Integer newListingsThisWeek;
    private Integer priceReducedThisWeek;
    private Double p25;
    private Double p75;
    private Double p90;

}