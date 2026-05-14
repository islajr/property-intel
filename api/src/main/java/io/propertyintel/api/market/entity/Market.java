package io.propertyintel.api.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(schema = "market", name = "neighbourhood_snapshots")
@Getter
@Setter
public class Market {

    @Id
    private String id;
    private String neighbourhood;
    private String city;
    private LocalDate snapshotWeek;
    private Double avgDaysOnMarket;
    private Instant computedAt;
    private Double medianPriceKobo;
    private Integer activeListingCount;
    private Integer newListingsCount;
    private Integer priceReducedCount;
    private Double p25;
    private Double p75;
    private Double p90;

}