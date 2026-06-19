package io.propertyintel.api.listing.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import io.propertyintel.api.global.exception.exceptions.ResourceNotFoundException;
import io.propertyintel.api.global.util.CursorPagination;
import io.propertyintel.api.listing.dto.ListingData;
import io.propertyintel.api.listing.dto.ListingDetailResponse;
import io.propertyintel.api.listing.dto.ListingResponse;
import io.propertyintel.api.listing.dto.ListingSearchParams;
import io.propertyintel.api.listing.entity.Listing;
import io.propertyintel.api.listing.mapper.ListingMapper;
import io.propertyintel.api.listing.repository.ListingRepository;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingMapper listingMapper;

    @InjectMocks
    private ListingService listingService;

    private Listing sampleListing1;
    private Listing sampleListing2;

    @BeforeEach
    void setUp() {
        sampleListing1 = new Listing();
        sampleListing1.setId(10L);
        sampleListing1.setPriceKobo(5000000L);

        sampleListing2 = new Listing();
        sampleListing2.setId(20L);
        sampleListing2.setPriceKobo(6000000L);
    }

    @Test
    void testGetListingsWithoutCursor() {
        ListingSearchParams params = new ListingSearchParams();
        params.setLimit(2);

        Page<Listing> page = new PageImpl<>(List.of(sampleListing1, sampleListing2));
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(listingRepository.count(any(Specification.class))).thenReturn(2L);

        ListingData data1 = new ListingData(10L, "src", "url", "title", 5000000L, "5M", "FLAT", 3, 3, "Ikoyi", "Lagos", 6.0, 3.0, "ACTIVE");
        ListingData data2 = new ListingData(20L, "src", "url", "title", 6000000L, "6M", "FLAT", 3, 3, "Ikoyi", "Lagos", 6.0, 3.0, "ACTIVE");
        when(listingMapper.toListingData(sampleListing1)).thenReturn(data1);
        when(listingMapper.toListingData(sampleListing2)).thenReturn(data2);

        ListingResponse response = listingService.getListings(params);

        assertNotNull(response);
        assertEquals(2, response.data().size());
        assertFalse(response.meta().hasMore());
        assertNull(response.meta().nextCursor());
        assertEquals(2, response.meta().count());

        verify(listingRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(listingRepository).count(any(Specification.class));
    }

    @Test
    void testGetListingsWithCursor() {
        ListingSearchParams params = new ListingSearchParams();
        params.setLimit(1);
        String cursor = CursorPagination.encode(10L);
        params.setCursor(cursor);

        // Mock lookup of last seen listing
        when(listingRepository.findById(10L)).thenReturn(Optional.of(sampleListing1));

        // Mock returns 2 listings (meaning limit + 1 exists)
        Page<Listing> page = new PageImpl<>(List.of(sampleListing1, sampleListing2));
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(listingRepository.count(any(Specification.class))).thenReturn(10L);

        ListingData data1 = new ListingData(10L, "src", "url", "title", 5000000L, "5M", "FLAT", 3, 3, "Ikoyi", "Lagos", 6.0, 3.0, "ACTIVE");
        when(listingMapper.toListingData(sampleListing1)).thenReturn(data1);

        ListingResponse response = listingService.getListings(params);

        assertNotNull(response);
        assertEquals(1, response.data().size());
        assertTrue(response.meta().hasMore());
        assertNotNull(response.meta().nextCursor());
        assertEquals(CursorPagination.encode(10L), response.meta().nextCursor());
        assertEquals(10, response.meta().count());

        verify(listingRepository).findById(10L);
        verify(listingRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetListingsCursorNotFound() {
        ListingSearchParams params = new ListingSearchParams();
        params.setCursor(CursorPagination.encode(999L));

        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> listingService.getListings(params));
    }

    @Test
    void testGetListingSuccessful() {
        // 1. Mock setup: Repository lookup setup
        when(listingRepository.findListingById(100L)).thenReturn(Optional.of(sampleListing1));

        // 2. Mock setup: Mapper mapping setup
        ListingDetailResponse responseDto = new ListingDetailResponse(100L, "source", "url", "Sample Listing", 150L, "150M", "DUPLEX", 5, 5, "neighbourhood", "city", 1.0001, 1.0002, "ACTIVE", null);
        when(listingMapper.toDetailResponse(sampleListing1)).thenReturn(responseDto);

        // 3. Execution: Call the service method
        ListingDetailResponse result = listingService.getListing(100L);

        // 4. Assertions: Verify returned DTO properties are, in fact, correct
        assertNotNull(result);
        assertEquals(responseDto.id(), result.id());
        assertEquals(responseDto.title(), result.title());
        assertEquals(responseDto.propertyType(), result.propertyType());

        // 5. Verification: Verify interactions with Mock Objects occurred
        verify(listingRepository).findListingById(100L);
        verify(listingMapper).toDetailResponse(sampleListing1);
    }

    @Test
    void testGetListingFailed() {
        // 1. Mock setup: Repository lookup setup 
        when(listingRepository.findListingById(120L)).thenReturn(Optional.empty());

        // 2. Execution and Assertion
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class, () -> listingService.getListing(120L));

        // 3. Additional assertion
        assertEquals("Listing with id 120 not found", exception.getMessage());

        // 4. Verification
        verify(listingRepository).findListingById(120L);
    }

    @Test
    void testGetListingsDefaultLimit() {
        ListingSearchParams params = new ListingSearchParams();
        params.setLimit(null); // Will default to 20

        Page<Listing> page = new PageImpl<>(List.of(sampleListing1));
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(listingRepository.count(any(Specification.class))).thenReturn(1L);

        ListingData data = new ListingData(10L, "src", "url", "title", 5000000L, "5M", "FLAT", 3, 3, "Ikoyi", "Lagos", 6.0, 3.0, "ACTIVE");
        when(listingMapper.toListingData(sampleListing1)).thenReturn(data);

        // Execute
        ListingResponse response = listingService.getListings(params);

        // Verify limit of 20 was used -> Pageable should request limit + 1 = 21
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(listingRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(21, pageableCaptor.getValue().getPageSize());
        assertNotNull(response);
    }

    @Test
    void testGetListingsThrowsResourceNotFound() {
        ListingSearchParams params = new ListingSearchParams();

        // Mock repository returning empty page
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // Verify it throws ResourceNotFoundException when no listings found on first page
        assertThrows(ResourceNotFoundException.class, () -> listingService.getListings(params));
    }

    @Test
    void testGetListingsWithArgumentCaptorReference() {
        // Prepare search parameters with a custom sort
        ListingSearchParams params = new ListingSearchParams();
        params.setSort("price_desc");
        params.setLimit(5);

        // Set up the necessary mocks for getListings() to run
        Page<Listing> page = new PageImpl<>(List.of(sampleListing1));
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(listingRepository.count(any(Specification.class))).thenReturn(1L);

        ListingData data = new ListingData(10L, "src", "url", "title", 5000000L, "5M", "FLAT", 3, 3, "Ikoyi", "Lagos", 6.0, 3.0, "ACTIVE");
        when(listingMapper.toListingData(sampleListing1)).thenReturn(data);

        // Create the ArgumentCaptor to capture the Pageable parameter
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Execute the service method
        listingService.getListings(params);

        // Verify the repository was called and capture the passed Pageable
        verify(listingRepository).findAll(any(Specification.class), pageableCaptor.capture());
        
        Pageable capturedPageable = pageableCaptor.getValue();
        assertNotNull(capturedPageable);

        // Assert that the captured Pageable contains the correct Sort configuration
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "priceKobo")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        
        assertEquals(expectedSort, capturedPageable.getSort());
    }

    @Test
    void testResolveSortNewest() {
        Sort sort = listingService.resolveSort("newest");

        assertNotNull(sort);
        assertEquals(Sort.by(Sort.Direction.DESC, "firstSeenAt")
                .and(Sort.by(Sort.Direction.DESC, "id")), sort);
    }

    @Test
    void testResolveSortPriceAsc() {
        Sort sort = listingService.resolveSort("price_asc");

        assertNotNull(sort);
        assertEquals(Sort.by(Sort.Direction.ASC, "priceKobo")
                .and(Sort.by(Sort.Direction.ASC, "id")), sort);
    }

    @Test
    void testResolveSortUndefined() {
        Sort sort = listingService.resolveSort(null);

        assertNotNull(sort);
        assertEquals(Sort.by(Sort.Direction.DESC, "firstSeenAt")
                .and(Sort.by(Sort.Direction.DESC, "id")), sort);
    }

    @Test
    void testResolveSortInvalidThrowsException() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> listingService.resolveSort("invalid_sort"));

        assertTrue(exception.getMessage().contains("Invalid sort option"));
    }
}
