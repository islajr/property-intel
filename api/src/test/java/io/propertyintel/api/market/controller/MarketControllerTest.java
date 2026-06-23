package io.propertyintel.api.market.controller;

import io.propertyintel.api.IntegrationTestBase;
import io.propertyintel.api.market.entity.Market;
import io.propertyintel.api.market.repository.MarketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class MarketControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MarketRepository marketRepository;

    @BeforeEach
    void setUp() {
        marketRepository.deleteAll();

        // Seed some snapshots for "Ajah"
        Market snapshot1 = new Market();
        snapshot1.setId("id-1");
        snapshot1.setNeighbourhood("Ajah");
        snapshot1.setCity("Lagos");
        snapshot1.setSnapshotWeek(LocalDate.now());
        snapshot1.setAvgDaysOnMarket(12.5);
        snapshot1.setComputedAt(Instant.now());
        snapshot1.setMedianPriceKobo(25000000.0);
        snapshot1.setActiveListingCount(10);
        snapshot1.setNewListingsCount(3);
        snapshot1.setPriceReducedCount(2);
        snapshot1.setP25(20000000.0);
        snapshot1.setP75(30000000.0);
        snapshot1.setP90(35000000.0);

        Market snapshot2 = new Market();
        snapshot2.setId("id-2");
        snapshot2.setNeighbourhood("Ajah");
        snapshot2.setCity("Lagos");
        snapshot2.setSnapshotWeek(LocalDate.now().minusWeeks(1));
        snapshot2.setAvgDaysOnMarket(14.0);
        snapshot2.setComputedAt(Instant.now());
        snapshot2.setMedianPriceKobo(24000000.0);
        snapshot2.setActiveListingCount(8);
        snapshot2.setNewListingsCount(2);
        snapshot2.setPriceReducedCount(1);
        snapshot2.setP25(19000000.0);
        snapshot2.setP75(29000000.0);
        snapshot2.setP90(34000000.0);

        marketRepository.saveAll(List.of(snapshot1, snapshot2));
    }

    @Test
    void testGetMarketStatsSuccess() throws Exception {
        mockMvc.perform(get("/market/stats")
                        .param("neighbourhood", "Ajah")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighbourhood").value("Ajah"))
                .andExpect(jsonPath("$.medianPriceKobo").value(25000000.0));
    }

    @Test
    void testGetMarketTrendsSuccess() throws Exception {
        mockMvc.perform(get("/market/trends")
                        .param("neighbourhood", "Ajah")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighbourhood").value("Ajah"))
                .andExpect(jsonPath("$.weeks", hasSize(2)));
    }
}
