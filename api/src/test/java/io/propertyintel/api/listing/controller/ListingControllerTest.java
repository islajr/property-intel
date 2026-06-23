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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ListingControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingRepository listingRepository;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();

        // Seed some listings
        Listing listing1 = new Listing(
                1L, "ext-1", "propertypro", "http://example.com/1", "House in Lekki",
                "Beautiful Lekki home", 500000000L, false, "SALE", "HOUSE",
                4, 4, 350.0, "agent", "12 Road", "Lekki",
                true, "Lagos", 6.4281, 3.4219, true,
                "Agent A", false, "ACTIVE", false, 0,
                Instant.now(), Instant.now(), Instant.now(), null
        );

        Listing listing2 = new Listing(
                2L, "ext-2", "jiji", "http://example.com/2", "Flat in Yaba",
                "Nice cozy flat", 150000000L, false, "RENT", "FLAT",
                2, 2, 120.0, "agent", "5 Street", "Yaba",
                true, "Lagos", 6.5244, 3.3792, true,
                "Agent B", false, "ACTIVE", false, 0,
                Instant.now(), Instant.now(), Instant.now(), null
        );

        listingRepository.saveAll(List.of(listing1, listing2));
    }

    @Test
    void testGetListingsSuccess() throws Exception {
        mockMvc.perform(get("/listings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void testGetListingByIdSuccess() throws Exception {
        mockMvc.perform(get("/listings/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("House in Lekki"));
    }

    @Test
    void testGetListingByIdNotFound() throws Exception {
        mockMvc.perform(get("/listings/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetListingsWithNeighbourhoodFilter() throws Exception {
        mockMvc.perform(get("/listings")
                        .param("neighbourhood", "Lekki")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void testGetListingsWithCursor() throws Exception {
        // First request to get cursor
        mockMvc.perform(get("/listings")
                        .param("limit", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.meta.has_more").value(true));
    }
}
