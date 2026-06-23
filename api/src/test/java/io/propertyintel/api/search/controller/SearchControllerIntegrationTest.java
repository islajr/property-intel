package io.propertyintel.api.search.controller;

import io.propertyintel.api.BaseControllerIntegrationTest;
import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingResponseMeta;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
class SearchControllerIntegrationTest extends BaseControllerIntegrationTest {

    @MockitoBean
    private SearchService searchService;

    @Test
    void testBasicSearchSuccess() throws Exception {
        ListingResponse mockResponse = new ListingResponse(
                Collections.emptyList(),
                new ListingResponseMeta(0, null, false)
        );

        when(searchService.performSearch(any(ListingSearchParams.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/search")
                .param("neighbourhood", "Ajah")
                .param("minPrice", "5000000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.meta.has_more").value(false));

        verify(searchService).performSearch(any(ListingSearchParams.class));
    }
}
