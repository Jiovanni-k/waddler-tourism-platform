package org.example.rest.hotel;

import org.example.rest.amenity.AmenityDto;
import org.example.rest.amenity.AmenityRepository;
import org.example.rest.amenity.Amenity;
import org.example.rest.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private AmenityRepository amenityRepository;

    @InjectMocks
    private HotelServiceImpl service;

    private Hotel hotel;
    private HotelRequestDto.CreateRequest createRequest;
    private HotelRequestDto.UpdateRequest updateRequest;
    private Amenity amenity;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setCity("New York");
        hotel.setRegion("Manhattan");
        hotel.setManagerId(100L);
        hotel.setStatus(HotelStatus.ACTIVE);
        hotel.setStarRating(5);
        hotel.setAmenities(new HashSet<>());
        hotel.setGalleryImageUrls(new ArrayList<>());

        createRequest = new HotelRequestDto.CreateRequest();
        createRequest.setName("Test Hotel");
        createRequest.setCity("New York");
        createRequest.setRegion("Manhattan");
        createRequest.setStarRating(5);

        updateRequest = new HotelRequestDto.UpdateRequest();
        updateRequest.setName("Updated Hotel");
        updateRequest.setStarRating(4);

        amenity = new Amenity();
        amenity.setId(1L);
        amenity.setName("WiFi");
    }

    @Test
    void testCreateHotel_Success() {
        Long managerId = 100L;
        createRequest.setAmenityIds(Set.of(1L));

        when(amenityRepository.findAllById(Set.of(1L))).thenReturn(List.of(amenity));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        HotelResponseDto.DetailResponse result = service.createHotel(createRequest, managerId);

        assertNotNull(result);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
        verify(amenityRepository, times(1)).findAllById(Set.of(1L));
    }

    @Test
    void testCreateHotel_WithoutAmenities() {
        Long managerId = 100L;
        createRequest.setAmenityIds(null);

        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        HotelResponseDto.DetailResponse result = service.createHotel(createRequest, managerId);

        assertNotNull(result);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
        verify(amenityRepository, never()).findAllById(any());
    }

    @Test
    void testUpdateHotel_Success_ByOwner() {
        Long hotelId = 1L;
        Long managerId = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            HotelResponseDto.DetailResponse result = service.updateHotel(hotelId, updateRequest, managerId);

            assertNotNull(result);
            verify(hotelRepository, times(1)).save(any(Hotel.class));
        }
    }

    @Test
    void testUpdateHotel_Success_ByAdmin() {
        Long hotelId = 1L;
        Long adminId = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            HotelResponseDto.DetailResponse result = service.updateHotel(hotelId, updateRequest, adminId);

            assertNotNull(result);
            verify(hotelRepository, times(1)).save(any(Hotel.class));
        }
    }

    @Test
    void testUpdateHotel_NotFound() {
        Long hotelId = 999L;
        Long managerId = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class,
                () -> service.updateHotel(hotelId, updateRequest, managerId));

        verify(hotelRepository, never()).save(any());
    }

    @Test
    void testUpdateHotel_AccessDenied() {
        Long hotelId = 1L;
        Long differentManagerId = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.updateHotel(hotelId, updateRequest, differentManagerId));

            verify(hotelRepository, never()).save(any());
        }
    }

    @Test
    void testUpdateHotel_WithAmenities() {
        Long hotelId = 1L;
        Long managerId = 100L;

        updateRequest.setAmenityIds(Set.of(1L, 2L));

        Amenity amenity2 = new Amenity();
        amenity2.setId(2L);
        amenity2.setName("Pool");

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(amenityRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(amenity, amenity2));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            HotelResponseDto.DetailResponse result = service.updateHotel(hotelId, updateRequest, managerId);

            assertNotNull(result);
            verify(hotelRepository, times(1)).save(any(Hotel.class));
            verify(amenityRepository, times(1)).findAllById(Set.of(1L, 2L));
        }
    }

    @Test
    void testDeleteHotel_Success() {
        Long hotelId = 1L;
        Long managerId = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            service.deleteHotel(hotelId, managerId);

            verify(hotelRepository, times(1)).save(any(Hotel.class));
            assertEquals(HotelStatus.INACTIVE, hotel.getStatus());
        }
    }

    @Test
    void testDeleteHotel_AccessDenied() {
        Long hotelId = 1L;
        Long differentManagerId = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.deleteHotel(hotelId, differentManagerId));

            verify(hotelRepository, never()).save(any());
        }
    }

    @Test
    void testGetHotelById_Success() {
        Long hotelId = 1L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        HotelResponseDto.DetailResponse result = service.getHotelById(hotelId);

        assertNotNull(result);
        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void testGetHotelById_NotFound() {
        Long hotelId = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class,
                () -> service.getHotelById(hotelId));
    }

    @Test
    void testListHotels_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hotel> page = new PageImpl<>(List.of(hotel), pageable, 1);

        when(hotelRepository.findAll((Specification<Hotel>) any(Specification.class), eq(pageable)))
                .thenReturn(page);

        Page<HotelResponseDto.SummaryResponse> result = service.listHotels(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(hotelRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testListHotels_WithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hotel> page = new PageImpl<>(List.of(hotel), pageable, 1);

        when(hotelRepository.findAll((Specification<Hotel>) any(Specification.class), eq(pageable)))
                .thenReturn(page);

        Page<HotelResponseDto.SummaryResponse> result = service.listHotels(
                "New York", 4, BigDecimal.valueOf(4.0), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testListHotels_InvalidCity() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.listHotels("   ", null, null, pageable));

        verify(hotelRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testListHotels_InvalidMinStars() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.listHotels(null, 6, null, pageable));

        verify(hotelRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testListHotels_InvalidMinRating() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.listHotels(null, null, BigDecimal.valueOf(6), pageable));

        verify(hotelRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchHotels_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hotel> page = new PageImpl<>(List.of(hotel), pageable, 1);

        when(hotelRepository.findAll((Specification<Hotel>) any(Specification.class), eq(pageable)))
                .thenReturn(page);

        Page<HotelResponseDto.SummaryResponse> result = service.searchHotels("Test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(hotelRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchHotels_EmptyQuery() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.searchHotels("   ", pageable));

        verify(hotelRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetHotelsByManager_Success() {
        Long managerId = 100L;

        when(hotelRepository.findByManagerId(managerId)).thenReturn(List.of(hotel));

        List<HotelResponseDto.SummaryResponse> result = service.getHotelsByManager(managerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(hotelRepository, times(1)).findByManagerId(managerId);
    }

    @Test
    void testGetHotelsByManager_Empty() {
        Long managerId = 999L;

        when(hotelRepository.findByManagerId(managerId)).thenReturn(List.of());

        List<HotelResponseDto.SummaryResponse> result = service.getHotelsByManager(managerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testApproveHotel_Success() {
        Long hotelId = 1L;

        hotel.setStatus(HotelStatus.PENDING_APPROVAL);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        HotelResponseDto.DetailResponse result = service.approveHotel(hotelId);

        assertNotNull(result);
        assertEquals(HotelStatus.ACTIVE, hotel.getStatus());
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testApproveHotel_InvalidStatus() {
        Long hotelId = 1L;

        hotel.setStatus(HotelStatus.ACTIVE);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        assertThrows(IllegalArgumentException.class,
                () -> service.approveHotel(hotelId));

        verify(hotelRepository, never()).save(any());
    }

    @Test
    void testSuspendHotel_Success() {
        Long hotelId = 1L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        HotelResponseDto.DetailResponse result = service.suspendHotel(hotelId);

        assertNotNull(result);
        assertEquals(HotelStatus.SUSPENDED, hotel.getStatus());
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testReactivateHotel_Success() {
        Long hotelId = 1L;

        hotel.setStatus(HotelStatus.SUSPENDED);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        HotelResponseDto.DetailResponse result = service.reactivateHotel(hotelId);

        assertNotNull(result);
        assertEquals(HotelStatus.ACTIVE, hotel.getStatus());
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testReactivateHotel_InvalidStatus() {
        Long hotelId = 1L;

        hotel.setStatus(HotelStatus.ACTIVE);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        assertThrows(IllegalArgumentException.class,
                () -> service.reactivateHotel(hotelId));

        verify(hotelRepository, never()).save(any());
    }

    @Test
    void testGetPendingHotels_Success() {
        Hotel pendingHotel = new Hotel();
        pendingHotel.setStatus(HotelStatus.PENDING_APPROVAL);

        when(hotelRepository.findAllByStatus(HotelStatus.PENDING_APPROVAL))
                .thenReturn(List.of(pendingHotel));

        List<HotelResponseDto.SummaryResponse> result = service.getPendingHotels();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(hotelRepository, times(1)).findAllByStatus(HotelStatus.PENDING_APPROVAL);
    }

    @Test
    void testGetPendingHotels_Empty() {
        when(hotelRepository.findAllByStatus(HotelStatus.PENDING_APPROVAL))
                .thenReturn(List.of());

        List<HotelResponseDto.SummaryResponse> result = service.getPendingHotels();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetHotelAmenities_Success() {
        Long hotelId = 1L;

        hotel.getAmenities().add(amenity);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        List<AmenityDto> result = service.getHotelAmenities(hotelId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void testGetHotelAmenities_Empty() {
        Long hotelId = 1L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        List<AmenityDto> result = service.getHotelAmenities(hotelId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddPhotoToHotel_Success() {
        Long hotelId = 1L;
        Long managerId = 100L;
        String photoUrl = "https://example.com/photo.jpg";

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            service.addPhotoToHotel(hotelId, photoUrl, managerId);

            assertTrue(hotel.getGalleryImageUrls().contains(photoUrl));
            verify(hotelRepository, times(1)).save(any(Hotel.class));
        }
    }

    @Test
    void testAddPhotoToHotel_AccessDenied() {
        Long hotelId = 1L;
        Long differentManagerId = 999L;
        String photoUrl = "https://example.com/photo.jpg";

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.addPhotoToHotel(hotelId, photoUrl, differentManagerId));

            assertFalse(hotel.getGalleryImageUrls().contains(photoUrl));
        }
    }

    @Test
    void testValidateSortBy_Valid() {
        assertDoesNotThrow(() -> service.validateSortBy("name"));
    }

    @Test
    void testValidateSortBy_Invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validateSortBy("invalidField"));
    }

    @Test
    void testValidateSortDir_ValidAsc() {
        assertDoesNotThrow(() -> service.validateSortDir("asc"));
    }

    @Test
    void testValidateSortDir_ValidDesc() {
        assertDoesNotThrow(() -> service.validateSortDir("DESC"));
    }

    @Test
    void testValidateSortDir_Invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validateSortDir("invalid"));
    }
}
