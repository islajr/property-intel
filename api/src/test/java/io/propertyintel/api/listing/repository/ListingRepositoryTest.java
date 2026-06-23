package io.propertyintel.api.listing.repository;

import io.propertyintel.api.BaseDatabaseTest;
import io.propertyintel.api.listing.entity.Listing;
import io.propertyintel.api.listing.util.ListingSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ListingRepositoryTest extends BaseDatabaseTest {

    @Autowired
    private ListingRepository listingRepository;

    @Test
    void testSaveAndFindListingById() {
        Listing listing = new Listing();
        listing.setId(1L);
        listing.setExternalId("ext-1");
        listing.setSource("jiji");
        listing.setUrl("http://jiji.ng/1");
        listing.setTitle("Beautiful Apartment");
        listing.setPriceKobo(10000000L);
        listing.setPropertyType("APARTMENT");
        listing.setNeighbourhood("Ajah");
        listing.setListingStatus("ACTIVE");
        listing.setFirstSeenAt(Instant.now());
        listing.setLastSeenAt(Instant.now());
        listing.setLastHealthCheckAt(Instant.now());

        listingRepository.save(listing);

        Optional<Listing> foundListing = listingRepository.findListingById(1L);
        assertTrue(foundListing.isPresent());
        assertEquals("Ajah", foundListing.get().getNeighbourhood());
    }

    @Test
    void testSpecificationsFilters() {
        Listing listing1 = new Listing();
        listing1.setId(2L);
        listing1.setExternalId("ext-2");
        listing1.setSource("jiji");
        listing1.setUrl("http://jiji.ng/2");
        listing1.setTitle("Ajah Duplex");
        listing1.setPriceKobo(50000000L);
        listing1.setPropertyType("DUPLEX");
        listing1.setNeighbourhood("Ajah");
        listing1.setListingStatus("ACTIVE");
        listing1.setFirstSeenAt(Instant.now());
        listing1.setLastSeenAt(Instant.now());
        listing1.setLastHealthCheckAt(Instant.now());

        Listing listing2 = new Listing();
        listing2.setId(3L);
        listing2.setExternalId("ext-3");
        listing2.setSource("jiji");
        listing2.setUrl("http://jiji.ng/3");
        listing2.setTitle("Ikoyi Flat");
        listing2.setPriceKobo(150000000L);
        listing2.setPropertyType("FLAT");
        listing2.setNeighbourhood("Ikoyi");
        listing2.setListingStatus("ACTIVE");
        listing2.setFirstSeenAt(Instant.now());
        listing2.setLastSeenAt(Instant.now());
        listing2.setLastHealthCheckAt(Instant.now());

        listingRepository.save(listing1);
        listingRepository.save(listing2);

        // Test Neighbourhood spec
        Specification<Listing> specNeighbourhood = ListingSpecifications.hasNeighbourhood("Ajah");
        List<Listing> listings = listingRepository.findAll(specNeighbourhood);
        assertEquals(1, listings.size());
        assertEquals(2L, listings.get(0).getId());

        // Test Min Price spec
        Specification<Listing> specMinPrice = ListingSpecifications.minPrice(60000000L);
        List<Listing> listingsMinPrice = listingRepository.findAll(specMinPrice);
        assertEquals(1, listingsMinPrice.size());
        assertEquals(3L, listingsMinPrice.get(0).getId());

        // Test Property Type spec
        Specification<Listing> specType = ListingSpecifications.hasType("FLAT");
        List<Listing> listingsType = listingRepository.findAll(specType);
        assertEquals(1, listingsType.size());
        assertEquals(3L, listingsType.get(0).getId());
    }
}
