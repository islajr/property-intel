package io.propertyintel.api.alert.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertRequest {
    @NotBlank(message = "Neighbourhood is required")
    private String neighbourhood;
    private Long maxPriceKobo;
    private Integer minBedrooms;
    private String propertyType;
}
