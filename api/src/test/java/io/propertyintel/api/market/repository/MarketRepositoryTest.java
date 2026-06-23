package io.propertyintel.api.market.repository;

import io.propertyintel.api.BaseDatabaseTest;
import io.propertyintel.api.listing.entity.Listing;
import io.propertyintel.api.listing.repository.ListingRepository;
import io.propertyintel.api.market.entity.Market;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MarketRepositoryTest extends BaseDatabaseTest {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Test
    void testSaveAndFindLatestRecordByNeighbourhood() {
        Market snapshot1 = new Market();
        snapshot1.setId("snap-1");
        snapshot1.setNeighbourhood("Ajah");
        snapshot1.setCity("Lagos");
        snapshot1.setSnapshotWeek(LocalDate.now().minusWeeks(1));
        snapshot1.setMedianPriceKobo(15000000.0);
        snapshot1.setComputedAt(Instant.now());

        Market snapshot2 = new Market();
        snapshot2.setId("snap-2");
        snapshot2.setNeighbourhood("Ajah");
        snapshot2.setCity("Lagos");
        snapshot2.setSnapshotWeek(LocalDate.now()); // newer snapshot week
        snapshot2.setMedianPriceKobo(16000000.0);
        snapshot2.setComputedAt(Instant.now());

        marketRepository.save(snapshot1);
        marketRepository.save(snapshot2);

        Optional<Market> latest = marketRepository.findLatestRecordByNeighbourhood("Ajah");
        assertTrue(latest.isPresent());
        assertEquals("snap-2", latest.get().getId());
        assertEquals(16000000.0, latest.get().getMedianPriceKobo());
    }

    @Test
    void testFindMarketTrends() {
        // Create 13 snapshots to test the limit of 12
        for (int i = 0; i < 13; i++) {
            Market snapshot = new Market();
            snapshot.setId("trend-" + i);
            snapshot.setNeighbourhood("Ikoyi");
            snapshot.setCity("Lagos");
            snapshot.setSnapshotWeek(LocalDate.now().minusWeeks(i));
            snapshot.setMedianPriceKobo(50000000.0 + i * 100000);
            snapshot.setComputedAt(Instant.now());
            marketRepository.save(snapshot);
        }

        List<Market> trends = marketRepository.findMarketTrends("Ikoyi");
        // Limit is 12
        assertEquals(12, trends.size());
        // Ordered by snapshot_week DESC, so index 0 should be the latest week (trend-0)
        assertEquals("trend-0", trends.get(0).getId());
    }

    @Test
    void testRefreshMarketSnapshot() {
        // Clear repositories first to have clean context
        marketRepository.deleteAll();
        listingRepository.deleteAll();

        Listing listing = new Listing();
        listing.setId(100L);
        listing.setExternalId("ext-100");
        listing.setSource("jiji");
        listing.setUrl("http://jiji.ng/100");
        listing.setTitle("Ajah Duplex");
        listing.setPriceKobo(45000000L);
        listing.setPropertyType("DUPLEX");
        listing.setNeighbourhood("Ajah");
        listing.setCity("Lagos");
        listing.setListingStatus("ACTIVE");
        listing.setFirstSeenAt(Instant.now().minusSeconds(86400 * 2));
        listing.setLastSeenAt(Instant.now());
        listing.setLastHealthCheckAt(Instant.now());

        listingRepository.save(listing);

        // Run refresh
        marketRepository.refreshMarketSnapshot();

        List<Market> snapshots = marketRepository.findAll();
        // Since we saved 1 active listing in Ajah, Lagos, a snapshot should be refreshed/inserted
        assertFalse(snapshots.isEmpty());
        
        Optional<Market> ajahSnapshot = snapshots.stream()
                .filter(s -> "Ajah".equals(s.getNeighbourhood()))
                .findFirst();
        assertTrue(ajahSnapshot.isPresent());
        assertEquals("Lagos", ajahSnapshot.get().getCity());
        assertEquals(1, ajahSnapshot.get().getActiveListingCount());
    }
}
