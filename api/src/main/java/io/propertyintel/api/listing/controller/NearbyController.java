package io.propertyintel.api.listing.controller;

import io.propertyintel.api.listing.dto.ListingData;
import io.propertyintel.api.listing.dto.NearbyRequest;
import io.propertyintel.api.listing.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NearbyController {

    private final ListingService listingService;

    @PostMapping({"/api/v1/listings/nearby", "/listings/nearby"})
    public ResponseEntity<List<ListingData>> getNearbyListings(@Valid @RequestBody NearbyRequest request) {
        return ResponseEntity.ok(listingService.getNearbyListings(request));
    }
}
