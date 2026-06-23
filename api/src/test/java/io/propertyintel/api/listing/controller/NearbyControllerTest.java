package io.propertyintel.api.listing.controller;

import io.propertyintel.api.IntegrationTestBase;
import io.propertyintel.api.listing.entity.Listing;
import io.propertyintel.api.listing.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class NearbyControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingRepository listingRepository;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();

        // Seed listings at specific coordinates
        // Lekki listing: 6.4281, 3.4219
        Listing lekkiListing = new Listing(
                1L, "ext-1", "propertypro", "http://example.com/1", "Lekki Apartment",
                "Beautiful Lekki home", 500000000L, false, "SALE", "HOUSE",
                4, 4, 350.0, "agent", "12 Road", "Lekki",
                true, "Lagos", 6.4281, 3.4219, true,
                "Agent A", false, "ACTIVE", false, 0,
                Instant.now(), Instant.now(), Instant.now(), null
        );

        // Yaba listing: 6.5244, 3.3792 (around 12.8km away from Lekki)
        Listing yabaListing = new Listing(
                2L, "ext-2", "jiji", "http://example.com/2", "Yaba Flat",
                "Cozy Yaba flat", 150000000L, false, "RENT", "FLAT",
                2, 2, 120.0, "agent", "5 Street", "Yaba",
                true, "Lagos", 6.5244, 3.3792, true,
                "Agent B", false, "ACTIVE", false, 0,
                Instant.now(), Instant.now(), Instant.now(), null
        );

        listingRepository.saveAll(List.of(lekkiListing, yabaListing));
    }

    @Test
    void testPostNearbyListingsSuccess() throws Exception {
        // Look within 2000m of Lekki (6.4281, 3.4219)
        String requestJson = """
                {
                    "lat": 6.4281,
                    "lng": 3.4219,
                    "radius_metres": 2000.0,
                    "limit": 5
                }
                """;

        mockMvc.perform(post("/listings/nearby")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testPostNearbyListingsInvalidCoordinates() throws Exception {
        // Invalid latitude 95.0
        String requestJson = """
                {
                    "lat": 95.0,
                    "lng": 3.4219,
                    "radius_metres": 2000.0,
                    "limit": 5
                }
                """;

        mockMvc.perform(post("/listings/nearby")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}
