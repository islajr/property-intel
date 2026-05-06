package io.propertyintel.api.listing;

import io.propertyintel.api.listing.dto.ListingDetailResponse;
import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @GetMapping("/api/v1/listings")
    public ResponseEntity<ListingResponse> getListings(@Valid ListingSearchParams searchParams, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);    // TODO: stage 1 - handle exceptions properly
        }
        return listingService.getListings(searchParams);
    }

    @GetMapping("/api/v1/listings/{id}")
    public ResponseEntity<ListingDetailResponse> getListing(@PathVariable Long id) {
        return listingService.getListing(id);
    }
}