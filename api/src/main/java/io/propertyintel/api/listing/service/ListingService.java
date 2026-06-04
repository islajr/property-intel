package io.propertyintel.api.listing.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import io.propertyintel.api.global.caching.CacheNames;
import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import io.propertyintel.api.global.exception.exceptions.ResourceNotFoundException;
import io.propertyintel.api.global.util.CursorPagination;
import io.propertyintel.api.listing.dto.ListingData;
import io.propertyintel.api.listing.dto.ListingDetailResponse;
import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingResponseMeta;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.listing.entity.Listing;
import io.propertyintel.api.listing.mapper.ListingMapper;
import io.propertyintel.api.listing.repository.ListingRepository;
import static io.propertyintel.api.listing.util.ListingSpecifications.afterCursor;
import static io.propertyintel.api.listing.util.ListingSpecifications.hasNeighbourhood;
import static io.propertyintel.api.listing.util.ListingSpecifications.hasReducedPrice;
import static io.propertyintel.api.listing.util.ListingSpecifications.hasType;
import static io.propertyintel.api.listing.util.ListingSpecifications.includeInactive;
import static io.propertyintel.api.listing.util.ListingSpecifications.maxBeds;
import static io.propertyintel.api.listing.util.ListingSpecifications.maxDays;
import static io.propertyintel.api.listing.util.ListingSpecifications.maxPrice;
import static io.propertyintel.api.listing.util.ListingSpecifications.minBeds;
import static io.propertyintel.api.listing.util.ListingSpecifications.minPrice;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    @Cacheable(value = CacheNames.LISTING_DETAILS, key = "#id")
    public ListingDetailResponse getListing(Long id) {

        /* Return details of the requested listing and an error if not found */

        Listing listing = listingRepository.findListingById(id).orElseThrow(
                () -> new ResourceNotFoundException("Listing with id %d not found".formatted(id)));

        System.out.println(listing.getPriceHistory().getFirst().getEventDate());
        return listingMapper.toDetailResponse(listing);
    }

    public ListingResponse getListings(ListingSearchParams searchParams) {

        Specification<Listing> specWithoutCursor = Specification
                .where(hasNeighbourhood(searchParams.getNeighbourhood()))
                .and(hasType(searchParams.getType()))
                .and(minBeds(searchParams.getMin_beds()))
                .and(maxBeds(searchParams.getMax_beds()))
                .and(minPrice(searchParams.getMin_price()))
                .and(maxPrice(searchParams.getMax_price()))
                .and(maxDays(searchParams.getMax_days()))
                .and(hasReducedPrice(searchParams.getPrice_reduced()))
                .and(includeInactive(searchParams.getInclude_inactive()));

        Listing lastListing = null;
        if (searchParams.getCursor() != null && !searchParams.getCursor().isBlank()) {
            Long lastId = CursorPagination.decode(searchParams.getCursor());
            if (lastId != null) {
                lastListing = listingRepository.findById(lastId)
                        .orElseThrow(() -> new BadRequestException("Invalid cursor: listing not found"));
            }
        }

        Specification<Listing> specWithCursor = specWithoutCursor.and(afterCursor(lastListing, searchParams.getSort()));

        Sort sort = resolveSort(searchParams.getSort());
        int limit = searchParams.getLimit() != null ? searchParams.getLimit() : 20;
        Pageable pageable = PageRequest.of(0, limit + 1, sort);

        Page<Listing> listingPage = listingRepository.findAll(specWithCursor, pageable);

        List<Listing> content = listingPage.getContent();
        if (content.isEmpty() && lastListing == null) {
            throw new ResourceNotFoundException("No listings found");
        }

        boolean hasMore = content.size() > limit;
        List<Listing> finalContent = hasMore ? content.subList(0, limit) : content;

        String nextCursor = null;
        if (!finalContent.isEmpty() && hasMore) {
            Listing nextLastListing = finalContent.get(finalContent.size() - 1);
            nextCursor = CursorPagination.encode(nextLastListing.getId());
        }

        long totalCount = listingRepository.count(specWithoutCursor);

        ListingResponseMeta meta = new ListingResponseMeta(
                (int) totalCount,
                nextCursor,
                hasMore
        );

        List<ListingData> data = finalContent.stream()
                .map(listingMapper::toListingData)
                .toList();

        return new ListingResponse(data, meta);
    }

    public Sort resolveSort(String sort) throws BadRequestException {
        return switch (sort == null ? "newest" : sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "priceKobo").and(Sort.by(Sort.Direction.ASC, "id"));
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "priceKobo").and(Sort.by(Sort.Direction.DESC, "id"));
            case "days_asc" -> Sort.by(Sort.Direction.ASC, "firstSeenAt").and(Sort.by(Sort.Direction.ASC, "id"));
            case "newest" -> Sort.by(Sort.Direction.DESC, "firstSeenAt").and(Sort.by(Sort.Direction.DESC, "id"));
            default -> throw new BadRequestException(
                    "Invalid sort option: %s. Valid options: price_asc, price_desc, newest, days_asc".formatted(sort)
            );
        };
    }
}