package io.propertyintel.api.listing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(schema = "raw_data", name = "listing_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PriceHistory {

    @Id
    private Long id;
    private Long listingId;
    private Long oldValue;
    private Long newValue;
    private String eventType;
    private LocalDate eventDate;
    private String notes;
}
