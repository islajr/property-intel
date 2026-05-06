package io.propertyintel.api.search;

import io.propertyintel.api.listing.dto.ListingSearchParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/api/v1/search")
    public ResponseEntity<?> basicSearch(ListingSearchParams params, BindingResult bindingResult) {
        if (bindingResult != null && bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        return searchService.performSearch(params);
    }
}