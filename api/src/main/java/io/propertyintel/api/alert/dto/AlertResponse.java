package io.propertyintel.api.alert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertResponse {
    private UUID id;
    private UUID userId;
    private String neighbourhood;
    private Long maxPriceKobo;
    private Integer minBedrooms;
    private String propertyType;
    private boolean isActive;
    private UUID alertUnsubscribeToken;
    private Instant createdAt;
}
