package io.propertyintel.api.listing.controller;

import io.propertyintel.api.BaseControllerIntegrationTest;
import io.propertyintel.api.listing.dto.ListingDetailResponse;
import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingResponseMeta;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.listing.service.ListingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
class ListingControllerIntegrationTest extends BaseControllerIntegrationTest {

    @MockitoBean
    private ListingService listingService;

    @Test
    void testGetListingsSuccess() throws Exception {
        ListingResponse mockResponse = new ListingResponse(
                Collections.emptyList(),
                new ListingResponseMeta(0, null, false)
        );

        when(listingService.getListings(any(ListingSearchParams.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/listings")
                .param("neighbourhood", "Ajah")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.meta.has_more").value(false));

        verify(listingService).getListings(any(ListingSearchParams.class));
    }

    @Test
    void testGetListingsValidationFailure() throws Exception {
        // limit > 100 or limit < 1 should trigger validation errors if configured on ListingSearchParams
        mockMvc.perform(get("/api/v1/listings")
                .param("limit", "-5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetListingByIdSuccess() throws Exception {
        ListingDetailResponse mockResponse = ListingDetailResponse.builder()
                .id(100L)
                .source("jiji")
                .url("http://jiji.ng/100")
                .title("Beautiful Apartment")
                .priceKobo(150000000L)
                .priceFormatted("₦150,000,000.00")
                .propertyType("DUPLEX")
                .bedrooms(4)
                .bathrooms(5)
                .neighbourhood("Ajah")
                .city("Lagos")
                .listingStatus("ACTIVE")
                .listingHistory(Collections.emptyList())
                .build();

        when(listingService.getListing(eq(100L))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/listings/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.neighbourhood").value("Ajah"))
                .andExpect(jsonPath("$.priceKobo").value(150000000L));

        verify(listingService).getListing(eq(100L));
    }
}
