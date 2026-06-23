package io.propertyintel.api.market.controller;

import io.propertyintel.api.BaseControllerIntegrationTest;
import io.propertyintel.api.market.dto.NeighbourhoodStatsResponse;
import io.propertyintel.api.market.dto.NeighbourhoodSummary;
import io.propertyintel.api.market.dto.NeighbourhoodSummaryMeta;
import io.propertyintel.api.market.dto.NeighbourhoodTrendResponse;
import io.propertyintel.api.market.service.MarketService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MarketController.class)
class MarketControllerIntegrationTest extends BaseControllerIntegrationTest {

    @MockitoBean
    private MarketService marketService;

    @Test
    void testGetNeighbourhoodsSuccess() throws Exception {
        NeighbourhoodSummary mockSummary = new NeighbourhoodSummary(
                Collections.emptyList(),
                new NeighbourhoodSummaryMeta(0, null, false)
        );

        when(marketService.getNeighbourhoods(eq("neighbourhood"), eq(20), eq("cursor-123")))
                .thenReturn(mockSummary);

        mockMvc.perform(get("/api/v1/market/neighbourhoods")
                .param("sort_by", "neighbourhood")
                .param("limit", "20")
                .param("cursor", "cursor-123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.meta.has_more").value(false));

        verify(marketService).getNeighbourhoods(eq("neighbourhood"), eq(20), eq("cursor-123"));
    }

    @Test
    void testGetNeighbourhoodsValidationMaxLimit() throws Exception {
        // limit > 50 should trigger validation error: Cannot request more than 50 items per page
        mockMvc.perform(get("/api/v1/market/neighbourhoods")
                .param("limit", "51")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetNeighbourhoodsValidationMinLimit() throws Exception {
        // limit < 1 should trigger validation error: Cannot request fewer than 1 item per page
        mockMvc.perform(get("/api/v1/market/neighbourhoods")
                .param("limit", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetNeighbourhoodStatsSuccess() throws Exception {
        NeighbourhoodStatsResponse mockResponse = new NeighbourhoodStatsResponse(
                "Ajah", "Lagos", 15000000L, "₦15,000,000.00", 10, 5.0, 3, 2, null
        );

        when(marketService.getNeighbourhoodStats(eq("Ajah"))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/market/Ajah/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighbourhood").value("Ajah"))
                .andExpect(jsonPath("$.medianPriceKobo").value(15000000L))
                .andExpect(jsonPath("$.formattedMedianPrice").value("₦15,000,000.00"));

        verify(marketService).getNeighbourhoodStats(eq("Ajah"));
    }

    @Test
    void testGetNeighbourhoodTrendsSuccess() throws Exception {
        NeighbourhoodTrendResponse mockResponse = new NeighbourhoodTrendResponse(
                "Ajah", Collections.emptyList()
        );

        when(marketService.getNeighbourhoodTrends(eq("Ajah"))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/market/neighbourhoods/Ajah/trends")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.neighbourhood").value("Ajah"))
                .andExpect(jsonPath("$.weeks").isArray());

        verify(marketService).getNeighbourhoodTrends(eq("Ajah"));
    }
}
