package org.example.rest.amenity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmenityServiceImplTest {

    @Mock
    private AmenityRepository amenityRepository;

    @InjectMocks
    private AmenityServiceImpl amenityService;

    private AmenityRequestDto requestDto;
    private Amenity amenity;

    @BeforeEach
    void setUp() {
        requestDto = AmenityRequestDto.builder()
                .name("WiFi")
                .description("Free wireless internet")
                .iconCode("wifi-icon")
                .category(AmenityCategory.WIFI)
                .build();

        amenity = Amenity.builder()
                .id(1L)
                .name("WiFi")
                .description("Free wireless internet")
                .iconCode("wifi-icon")
                .category(AmenityCategory.WIFI)
                .build();
    }

    @Test
    void testCreateAmenity_Success() {
        // Arrange
        when(amenityRepository.existsByName("WiFi")).thenReturn(false);
        when(amenityRepository.save(any(Amenity.class))).thenReturn(amenity);

        // Act
        AmenityResponseDto.DetailResponse result = amenityService.createAmenity(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("WiFi", result.getName());
        assertEquals("Free wireless internet", result.getDescription());
        assertEquals("wifi-icon", result.getIconCode());
        assertEquals(AmenityCategory.WIFI, result.getCategory());
        verify(amenityRepository, times(1)).existsByName("WiFi");
        verify(amenityRepository, times(1)).save(any(Amenity.class));
    }

    @Test
    void testCreateAmenity_DuplicateThrowsException() {
        // Arrange
        when(amenityRepository.existsByName("WiFi")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateAmenityException.class,
                () -> amenityService.createAmenity(requestDto));

        verify(amenityRepository, times(1)).existsByName("WiFi");
        verify(amenityRepository, never()).save(any(Amenity.class));
    }

    @Test
    void testCreateAmenity_WithDifferentCategories() {
        AmenityRequestDto poolDto = AmenityRequestDto.builder()
                .name("Swimming Pool")
                .description("Olympic size pool")
                .iconCode("pool-icon")
                .category(AmenityCategory.POOL)
                .build();

        Amenity poolAmenity = Amenity.builder()
                .id(2L)
                .name("Swimming Pool")
                .description("Olympic size pool")
                .iconCode("pool-icon")
                .category(AmenityCategory.POOL)
                .build();

        when(amenityRepository.existsByName("Swimming Pool")).thenReturn(false);
        when(amenityRepository.save(any(Amenity.class))).thenReturn(poolAmenity);

        // Act
        AmenityResponseDto.DetailResponse result = amenityService.createAmenity(poolDto);

        // Assert
        assertNotNull(result);
        assertEquals("Swimming Pool", result.getName());
        assertEquals(AmenityCategory.POOL, result.getCategory());
        verify(amenityRepository, times(1)).save(any(Amenity.class));
    }

    @Test
    void testUpdateAmenity_Success() {
        // Arrange
        Long amenityId = 1L;
        AmenityRequestDto updateDto = AmenityRequestDto.builder()
                .name("WiFi Pro")
                .description("High-speed wireless internet")
                .iconCode("wifi-fast")
                .category(AmenityCategory.WIFI)
                .build();

        Amenity updatedAmenity = Amenity.builder()
                .id(1L)
                .name("WiFi Pro")
                .description("High-speed wireless internet")
                .iconCode("wifi-fast")
                .category(AmenityCategory.WIFI)
                .build();

        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(amenity));
        when(amenityRepository.existsByName("WiFi Pro")).thenReturn(false);
        when(amenityRepository.save(any(Amenity.class))).thenReturn(updatedAmenity);

        AmenityResponseDto.DetailResponse result = amenityService.updateAmenity(amenityId, updateDto);

        assertNotNull(result);
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(amenityRepository, times(1)).existsByName("WiFi Pro");
        verify(amenityRepository, times(1)).save(any(Amenity.class));
    }

    @Test
    void testUpdateAmenity_ChangeCategory() {
        Long amenityId = 1L;
        AmenityRequestDto updateDto = AmenityRequestDto.builder()
                .name("WiFi")
                .description("Updated description")
                .iconCode("wifi-icon")
                .category(AmenityCategory.BUSINESS_CENTER)
                .build();

        Amenity updatedAmenity = Amenity.builder()
                .id(1L)
                .name("WiFi")
                .description("Updated description")
                .iconCode("wifi-icon")
                .category(AmenityCategory.BUSINESS_CENTER)
                .build();

        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(amenity));
        when(amenityRepository.save(any(Amenity.class))).thenReturn(updatedAmenity);

        AmenityResponseDto.DetailResponse result = amenityService.updateAmenity(amenityId, updateDto);

        assertNotNull(result);
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(amenityRepository, times(1)).save(any(Amenity.class));
    }

    @Test
    void testUpdateAmenity_NotFoundThrowsException() {
        Long amenityId = 999L;
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());

        assertThrows(AmenityNotFoundException.class,
                () -> amenityService.updateAmenity(amenityId, requestDto));

        verify(amenityRepository, never()).save(any(Amenity.class));
    }

    @Test
    void testUpdateAmenity_DuplicateNameThrowsException() {
        Long amenityId = 1L;
        AmenityRequestDto updateDto = AmenityRequestDto.builder()
                .name("Pool")
                .description("Swimming pool")
                .category(AmenityCategory.POOL)
                .build();

        Amenity existingAmenity = Amenity.builder()
                .id(1L)
                .name("WiFi")
                .description("Internet")
                .category(AmenityCategory.WIFI)
                .build();

        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(existingAmenity));
        when(amenityRepository.existsByName("Pool")).thenReturn(true);

        assertThrows(DuplicateAmenityException.class,
                () -> amenityService.updateAmenity(amenityId, updateDto));

        verify(amenityRepository, never()).save(any(Amenity.class));
    }

    @Test
    void testDeleteAmenity_Success() {
        Long amenityId = 1L;
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(amenity));

        amenityService.deleteAmenity(amenityId);

        verify(amenityRepository, times(1)).findById(amenityId);
        verify(amenityRepository, times(1)).delete(amenity);
    }

    @Test
    void testDeleteAmenity_NotFoundThrowsException() {
        Long amenityId = 999L;
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());

        assertThrows(AmenityNotFoundException.class,
                () -> amenityService.deleteAmenity(amenityId));

        verify(amenityRepository, never()).delete(any(Amenity.class));
    }

    @Test
    void testGetAmenityById_Success() {
        Long amenityId = 1L;
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(amenity));

        AmenityResponseDto.DetailResponse result = amenityService.getAmenityById(amenityId);

        assertNotNull(result);
        assertEquals("WiFi", result.getName());
        assertEquals(AmenityCategory.WIFI, result.getCategory());
        verify(amenityRepository, times(1)).findById(amenityId);
    }

    @Test
    void testGetAmenityById_NotFoundThrowsException() {
        Long amenityId = 999L;
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());

        assertThrows(AmenityNotFoundException.class,
                () -> amenityService.getAmenityById(amenityId));
    }

    @Test
    void testListAmenities_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Amenity> page = new PageImpl<>(List.of(amenity), pageable, 1);
        when(amenityRepository.findAll(pageable)).thenReturn(page);

        Page<AmenityResponseDto.Response> result = amenityService.listAmenities(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        verify(amenityRepository, times(1)).findAll(pageable);
    }

    @Test
    void testListAmenities_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Amenity> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(amenityRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<AmenityResponseDto.Response> result = amenityService.listAmenities(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllAmenities_Success() {
        Amenity amenity2 = Amenity.builder()
                .id(2L)
                .name("Swimming Pool")
                .description("Olympic size pool")
                .category(AmenityCategory.POOL)
                .build();

        Amenity amenity3 = Amenity.builder()
                .id(3L)
                .name("Gym")
                .description("Fitness center")
                .category(AmenityCategory.GYM)
                .build();

        when(amenityRepository.findAll()).thenReturn(List.of(amenity, amenity2, amenity3));

        List<AmenityResponseDto.Response> result = amenityService.getAllAmenities();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("WiFi", result.get(0).getName());
        assertEquals("Swimming Pool", result.get(1).getName());
        assertEquals("Gym", result.get(2).getName());
        verify(amenityRepository, times(1)).findAll();
    }

    @Test
    void testGetAllAmenities_Empty() {
        when(amenityRepository.findAll()).thenReturn(List.of());

        List<AmenityResponseDto.Response> result = amenityService.getAllAmenities();

        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateAmenity_WithSameName_Success() {
        // Edge case: updating with the same name should not throw duplicate exception
        Long amenityId = 1L;
        AmenityRequestDto updateDto = AmenityRequestDto.builder()
                .name("WiFi")  // Same name as existing
                .description("Updated description")
                .iconCode("wifi-icon-v2")
                .category(AmenityCategory.WIFI)
                .build();

        Amenity updatedAmenity = Amenity.builder()
                .id(1L)
                .name("WiFi")
                .description("Updated description")
                .iconCode("wifi-icon-v2")
                .category(AmenityCategory.WIFI)
                .build();

        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(amenity));
        when(amenityRepository.save(any(Amenity.class))).thenReturn(updatedAmenity);

        AmenityResponseDto.DetailResponse result = amenityService.updateAmenity(amenityId, updateDto);

        assertNotNull(result);
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(amenityRepository, times(1)).save(any(Amenity.class));
        // Should not call existsByName when name is the same
    }

    @Test
    void testListAmenities_WithMultiplePages() {
        // Test pagination with multiple pages
        Amenity amenity2 = Amenity.builder()
                .id(2L)
                .name("Swimming Pool")
                .description("Olympic size pool")
                .category(AmenityCategory.POOL)
                .build();

        Pageable pageable = PageRequest.of(0, 1);
        Page<Amenity> page = new PageImpl<>(List.of(amenity), pageable, 2);
        when(amenityRepository.findAll(pageable)).thenReturn(page);

        Page<AmenityResponseDto.Response> result = amenityService.listAmenities(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertTrue(result.hasNext());
        verify(amenityRepository, times(1)).findAll(pageable);
    }

    @Test
    void testCreateAmenity_WithAllFields() {
        // Comprehensive test with all fields populated
        AmenityRequestDto fullDto = AmenityRequestDto.builder()
                .name("Full Amenity")
                .description("Complete description with details")
                .iconCode("full-icon-code")
                .category(AmenityCategory.BUSINESS_CENTER)
                .build();

        Amenity fullAmenity = Amenity.builder()
                .id(5L)
                .name("Full Amenity")
                .description("Complete description with details")
                .iconCode("full-icon-code")
                .category(AmenityCategory.BUSINESS_CENTER)
                .build();

        when(amenityRepository.existsByName("Full Amenity")).thenReturn(false);
        when(amenityRepository.save(any(Amenity.class))).thenReturn(fullAmenity);

        AmenityResponseDto.DetailResponse result = amenityService.createAmenity(fullDto);

        assertNotNull(result);
        assertEquals("Full Amenity", result.getName());
        assertEquals("Complete description with details", result.getDescription());
        assertEquals("full-icon-code", result.getIconCode());
        assertEquals(AmenityCategory.BUSINESS_CENTER, result.getCategory());
        verify(amenityRepository, times(1)).save(any(Amenity.class));
    }

    @Test
    void testListAmenities_VerifyPaginationParameters() {
        // Verify that pagination parameters are correctly passed
        Pageable pageable = PageRequest.of(2, 5);  // page 2, size 5
        Page<Amenity> page = new PageImpl<>(List.of(amenity), pageable, 15);
        when(amenityRepository.findAll(pageable)).thenReturn(page);

        Page<AmenityResponseDto.Response> result = amenityService.listAmenities(pageable);

        assertNotNull(result);
        assertEquals(2, result.getNumber());  // page number
        assertEquals(5, result.getSize());    // page size
        assertEquals(15, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        verify(amenityRepository, times(1)).findAll(pageable);
    }
}