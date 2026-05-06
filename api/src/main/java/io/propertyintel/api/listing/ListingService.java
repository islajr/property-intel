package io.propertyintel.api.listing;

import io.propertyintel.api.listing.dto.ListingDetailResponse;
import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.listing.mapper.ListingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static io.propertyintel.api.listing.ListingSpecifications.*;


@Service
@RequiredArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    public ResponseEntity<ListingDetailResponse> getListing(Long id) {

        /* Return details of the requested listing and an error if not found */

        Listing listing = listingRepository.findListingById(id).orElseThrow(
                () -> new RuntimeException("Listing not found"));   // TODO: stage 1 - handle exceptions properly

        System.out.println(listing.getPriceHistory().getFirst().getEventDate());
        return ResponseEntity.ok(listingMapper.toDetailResponse(listing));
    }

    public ResponseEntity<ListingResponse> getListings(ListingSearchParams searchParams) {

        Specification<Listing> spec = Specification
                .where(hasNeighbourhood(searchParams.getNeighbourhood()))
                .and(hasType(searchParams.getType()))
                .and(minBeds(searchParams.getMin_beds()))
                .and(maxBeds(searchParams.getMax_beds()))
                .and(minPrice(searchParams.getMin_price()))
                .and(maxPrice(searchParams.getMax_price()))
                .and(maxDays(searchParams.getMax_days()))
                .and(hasReducedPrice(searchParams.getPrice_reduced()))
                .and(includeInactive(searchParams.getInclude_inactive()));


        Sort sort = resolveSort(searchParams.getSort());
        Pageable pageable = buildPageable(searchParams.getLimit(), sort, searchParams.getPage());

        Page<Listing> listingPage = listingRepository.findAll(spec, pageable);

        return ResponseEntity.ok(listingMapper.toResponse(listingPage));


    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "newest" : sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "priceKobo");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "priceKobo");
            case "days_asc" -> Sort.by(Sort.Direction.ASC, "firstSeenAt");  // TODO: REVIEW - questionable spec
            case "newest" -> Sort.by(Sort.Direction.DESC, "firstSeenAt");
            default -> throw new IllegalArgumentException(
                    "Invalid sort option: %s. Valid options: price_asc, price_desc, newest, days_asc".formatted(sort)
            );  // TODO: remember to handle exceptions properly
        };
    }

    private Pageable buildPageable(Integer limit, Sort sort, Integer pageNumber) {
        int size = (limit != null && limit > 0) ? limit : 20;   // default value
        return PageRequest.of(pageNumber, size, sort);
    }
}