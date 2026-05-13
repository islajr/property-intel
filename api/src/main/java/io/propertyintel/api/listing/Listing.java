package io.propertyintel.api.listing;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(schema = "raw_data", name = "scraped_listings")
@AllArgsConstructor
@NoArgsConstructor
public class Listing {
    @Id
    private Long id;
    private String externalId;
    private String source;
    private String url;
    private String title;
    private String description;
    private long priceKobo;
    private boolean priceParseFailed;
    private String priceType;
    private String propertyType;
    private Integer bedrooms;
    private Integer bathrooms;
    private Double floorAreaSqm;
    private String floorAreaSource;
    private String rawAddress;
    private String neighbourhood;
    private boolean neighbourhoodNormalised;
    private String city;
    private Double lat;
    private Double lng;
    private boolean geocoded;
    private String agentName;
    private boolean diasporaTargeted;
    private String listingStatus;
    private boolean suspectedSold;
    private int missedRunCount;
    private Instant firstSeenAt;
    private Instant lastSeenAt;
    private Instant lastHealthCheckAt;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "listingId", referencedColumnName = "id", insertable = false, updatable = false)
    private List<PriceHistory> priceHistory;

}
