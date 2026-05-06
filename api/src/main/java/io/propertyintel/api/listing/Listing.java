package io.propertyintel.api.listing;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastSeenAt;
    private LocalDateTime lastHealthCheckAt;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "listingId", referencedColumnName = "id", insertable = false, updatable = false)
    private List<PriceHistory> priceHistory;

}
