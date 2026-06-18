package io.propertyintel.api.listing.util;

import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

import io.propertyintel.api.listing.entity.Listing;
import io.propertyintel.api.listing.entity.PriceHistory;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class ListingSpecifications {

    public static Specification<Listing> hasNeighbourhood(String neighbourhood) {
        return (root, query, cb) ->
                neighbourhood == null ? null : cb.like(root.get("neighbourhood"), neighbourhood);
    }

    public static Specification<Listing> hasType(String type) {
        return (root, query, cb) ->
                type == null ? null : cb.like(root.get("propertyType"), type.toUpperCase());
    }

    public static Specification<Listing> minBeds(Integer minBeds) {
        return (root, query, cb) ->
                minBeds == null ? null : cb.greaterThanOrEqualTo(root.get("bedrooms"), minBeds);
    }

    public static Specification<Listing> maxBeds(Integer maxBeds) {
        return (root, query, cb) ->
                maxBeds == null ? null : cb.lessThanOrEqualTo(root.get("bedrooms"), maxBeds);
    }

    public static Specification<Listing> minPrice(Long minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("priceKobo"), minPrice);
    }

    public static Specification<Listing> maxPrice(Long maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("priceKobo"), maxPrice);
    }

    public static Specification<Listing> maxDays(Integer days) {
        return (root, query, cb) -> {
            if (days == null || days <= 0) return null;

            query.distinct(true);

            // Uses health checks as a signal for recency.
            // Should be okay as long as health checks are consistent.
            Expression<Instant> first = root.get("firstSeenAt");
            Expression<Instant> last = cb.coalesce(
                    root.get("lastHealthCheckAt"),
                    root.get("firstSeenAt")
            );


            // Use postgres-native 'DATE_PART' function to fetch the epoch value
            Expression<Double> lastEpoch = cb.function(
                    "DATE_PART", Double.class, cb.literal("epoch"), last
            );

            Expression<Double> firstEpoch = cb.function(
                    "DATE_PART", Double.class, cb.literal("epoch"), first
            );

            Expression<Double> secondsDiff = cb.diff(lastEpoch, firstEpoch);

            Expression<Number> daysDiff = cb.quot(secondsDiff, 86400.0);

            Expression<Integer> solidDays = cb.function("FLOOR", Integer.class, daysDiff);

            return cb.lessThanOrEqualTo(solidDays, days);
        };
    }

    public static Specification<Listing> hasReducedPrice(Boolean reducedPrice) {
        return (root, query, cb) -> {
            if (reducedPrice == null || !reducedPrice) return cb.conjunction(); // safe no-op predicate

            query.distinct(true);
            Join<Listing, PriceHistory> priceHistoryJoin = root.join("priceHistory", JoinType.INNER);


            return cb.and(
                    cb.equal(priceHistoryJoin.get("eventType"), "PRICE_CHANGE"),
                    cb.lessThan(priceHistoryJoin.get("newValue"), priceHistoryJoin.get("oldValue"))
            );

        };
    }

    public static Specification<Listing> includeInactive(Boolean includeInactive) {

        return (root, query, cb) -> {
            if (includeInactive != null && includeInactive) {
                return cb.or(
                        cb.equal(root.get("listingStatus"), "ACTIVE"),
                        cb.equal(root.get("listingStatus"), "REMOVED")
                );
            } else {
                return cb.equal(root.get("listingStatus"), "ACTIVE");
            }
        };
    }

    public static Specification<Listing> afterCursor(Listing lastListing, String sort) {
        return (root, query, cb) -> {
            if (lastListing == null) return null;

            String sortOption = sort == null ? "newest" : sort;
            return switch (sortOption) {
                case "price_asc" -> cb.or(
                        cb.greaterThan(root.get("priceKobo"), lastListing.getPriceKobo()),
                        cb.and(
                                cb.equal(root.get("priceKobo"), lastListing.getPriceKobo()),
                                cb.greaterThan(root.get("id"), lastListing.getId())
                        )
                );
                case "price_desc" -> cb.or(
                        cb.lessThan(root.get("priceKobo"), lastListing.getPriceKobo()),
                        cb.and(
                                cb.equal(root.get("priceKobo"), lastListing.getPriceKobo()),
                                cb.lessThan(root.get("id"), lastListing.getId())
                        )
                );
                case "days_asc" -> cb.or(
                        cb.greaterThan(root.get("firstSeenAt"), lastListing.getFirstSeenAt()),
                        cb.and(
                                cb.equal(root.get("firstSeenAt"), lastListing.getFirstSeenAt()),
                                cb.greaterThan(root.get("id"), lastListing.getId())
                        )
                );
                case "newest" -> cb.or(
                        cb.lessThan(root.get("firstSeenAt"), lastListing.getFirstSeenAt()),
                        cb.and(
                                cb.equal(root.get("firstSeenAt"), lastListing.getFirstSeenAt()),
                                cb.lessThan(root.get("id"), lastListing.getId())
                        )
                );
                default -> null;
            };
        };
    }
}
