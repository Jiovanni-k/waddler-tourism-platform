package org.example.rest.cancellationpolicy;

import org.example.rest.PagedResponse;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.room.RoomRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancellationPolicyServiceImplTest {

    @Mock
    private CancellationPolicyRepository repository;

    @Mock
    private CancellationPolicyMapper mapper;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private CancellationPolicyServiceImpl service;

    private Hotel hotel;
    private CancellationPolicy policy;
    private CancellationPolicyRequestDto requestDto;
    private CancellationPolicyResponseDto responseDto;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .managerId(100L)
                .build();

        policy = CancellationPolicy.builder()
                .id(1L)
                .hotel(hotel)
                .name(CancellationPolicyName.MODERATE)
                .description("Moderate cancellation policy")
                .daysBeforeCheckin(5)
                .refundPercentage(BigDecimal.valueOf(75.0))
                .build();

        requestDto = new CancellationPolicyRequestDto();
        requestDto.setName(CancellationPolicyName.MODERATE);
        requestDto.setDescription("Moderate cancellation policy");
        requestDto.setDaysBeforeCheckin(5);
        requestDto.setRefundPercentage(BigDecimal.valueOf(75.0));

        responseDto = new CancellationPolicyResponseDto();
        responseDto.setId(1L);
        responseDto.setHotelId(1L);
        responseDto.setHotelName("Test Hotel");
        responseDto.setName(CancellationPolicyName.MODERATE);
        responseDto.setDescription("Moderate cancellation policy");
        responseDto.setDaysBeforeCheckin(5);
        responseDto.setRefundPercentage(BigDecimal.valueOf(75.0));
    }


    @Test
    void testCreate_Success() {
        Long hotelId = 1L;
        Long managerId = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(mapper.toEntity(requestDto, hotel)).thenReturn(policy);
        when(repository.save(any(CancellationPolicy.class))).thenReturn(policy);
        when(mapper.toResponseDto(policy)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            CancellationPolicyResponseDto result = service.create(hotelId, managerId, requestDto);

            assertNotNull(result);
            assertEquals(responseDto.getId(), result.getId());
            assertEquals(responseDto.getName(), result.getName());
            verify(hotelRepository, times(1)).findById(hotelId);
            verify(repository, times(1)).save(any(CancellationPolicy.class));
        }
    }

    @Test
    void testCreate_HotelNotFound() {
        Long hotelId = 999L;
        Long managerId = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class,
                () -> service.create(hotelId, managerId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_DuplicatePolicyName() {
        Long hotelId = 1L;
        Long managerId = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(repository.findByHotel_IdAndName(hotelId, requestDto.getName()))
                .thenReturn(Optional.of(policy));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(DuplicateCancellationPolicyException.class,
                    () -> service.create(hotelId, managerId, requestDto));

            verify(repository, never()).save(any());
        }
    }

    @Test
    void testCreate_AccessDenied_NotOwner() {
        Long hotelId = 1L;
        Long managerId = 999L; // Different manager

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.create(hotelId, managerId, requestDto));

            verify(repository, never()).save(any());
        }
    }

    @Test
    void testCreate_AccessAllowed_Admin() {
        Long hotelId = 1L;
        Long managerId = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(repository.findByHotel_IdAndName(hotelId, requestDto.getName()))
                .thenReturn(Optional.empty());
        when(mapper.toEntity(requestDto, hotel)).thenReturn(policy);
        when(repository.save(any(CancellationPolicy.class))).thenReturn(policy);
        when(mapper.toResponseDto(policy)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            CancellationPolicyResponseDto result = service.create(hotelId, managerId, requestDto);

            assertNotNull(result);
            verify(repository, times(1)).save(any());
        }
    }

    @Test
    void testGetById_Success() {
        Long hotelId = 1L;
        Long policyId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(mapper.toResponseDto(policy)).thenReturn(responseDto);

        CancellationPolicyResponseDto result = service.getById(hotelId, policyId);

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(repository, times(1)).findById(policyId);
    }

    @Test
    void testGetById_HotelNotFound() {
        Long hotelId = 999L;
        Long policyId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        assertThrows(HotelNotFoundException.class,
                () -> service.getById(hotelId, policyId));
    }

    @Test
    void testGetById_PolicyNotFound() {
        Long hotelId = 1L;
        Long policyId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.empty());

        assertThrows(CancellationPolicyNotFoundException.class,
                () -> service.getById(hotelId, policyId));
    }

    @Test
    void testGetById_PolicyNotBelongToHotel() {
        Long hotelId = 1L;
        Long differentHotelId = 2L;
        Long policyId = 1L;

        Hotel differentHotel = Hotel.builder()
                .id(differentHotelId)
                .name("Different Hotel")
                .managerId(100L)
                .build();

        CancellationPolicy differentPolicy = CancellationPolicy.builder()
                .id(policyId)
                .hotel(differentHotel)
                .name(CancellationPolicyName.MODERATE)
                .build();

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(differentPolicy));

        assertThrows(CancellationPolicyNotBelongToHotelException.class,
                () -> service.getById(hotelId, policyId));
    }

    @Test
    void testList_Success() {
        Long hotelId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<CancellationPolicy> page = new PageImpl<>(List.of(policy), pageable, 1);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toResponseDto(policy)).thenReturn(responseDto);

        PagedResponse<CancellationPolicyResponseDto> result = service.list(
                hotelId, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testList_WithFilters() {
        Long hotelId = 1L;
        CancellationPolicyName name = CancellationPolicyName.MODERATE;
        Integer minDays = 3;
        Integer maxDays = 7;
        BigDecimal minRefund = BigDecimal.valueOf(50.0);
        BigDecimal maxRefund = BigDecimal.valueOf(100.0);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CancellationPolicy> page = new PageImpl<>(List.of(policy), pageable, 1);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toResponseDto(policy)).thenReturn(responseDto);

        PagedResponse<CancellationPolicyResponseDto> result = service.list(
                hotelId, name, minDays, maxDays, minRefund, maxRefund, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testList_HotelNotFound() {
        Long hotelId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        assertThrows(HotelNotFoundException.class,
                () -> service.list(hotelId, null, null, null, null, null, pageable));
    }

    @Test
    void testList_InvalidMinMaxDays() {
        Long hotelId = 1L;
        Integer minDays = 10;
        Integer maxDays = 5; // minDays > maxDays
        Pageable pageable = PageRequest.of(0, 10);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.list(hotelId, null, minDays, maxDays, null, null, pageable));
    }

    @Test
    void testList_InvalidMinMaxRefund() {
        Long hotelId = 1L;
        BigDecimal minRefund = BigDecimal.valueOf(100.0);
        BigDecimal maxRefund = BigDecimal.valueOf(50.0); // minRefund > maxRefund
        Pageable pageable = PageRequest.of(0, 10);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.list(hotelId, null, null, null, minRefund, maxRefund, pageable));
    }

    @Test
    void testList_EmptyResult() {
        Long hotelId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<CancellationPolicy> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        PagedResponse<CancellationPolicyResponseDto> result = service.list(
                hotelId, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testUpdate_Success() {
        Long hotelId = 1L;
        Long policyId = 1L;
        Long managerId = 100L;

        CancellationPolicyRequestDto updateDto = new CancellationPolicyRequestDto();
        updateDto.setName(CancellationPolicyName.FLEXIBLE);
        updateDto.setDescription("Updated description");
        updateDto.setDaysBeforeCheckin(3);
        updateDto.setRefundPercentage(BigDecimal.valueOf(90.0));

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(repository.findByHotel_IdAndName(hotelId, updateDto.getName()))
                .thenReturn(Optional.empty());
        when(repository.save(any(CancellationPolicy.class))).thenReturn(policy);
        when(mapper.toResponseDto(policy)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            CancellationPolicyResponseDto result = service.update(hotelId, policyId, managerId, updateDto);

            assertNotNull(result);
            verify(repository, times(1)).save(any(CancellationPolicy.class));
        }
    }

    @Test
    void testUpdate_HotelNotFound() {
        Long hotelId = 999L;
        Long policyId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        assertThrows(HotelNotFoundException.class,
                () -> service.update(hotelId, policyId, managerId, requestDto));
    }

    @Test
    void testUpdate_PolicyNotFound() {
        Long hotelId = 1L;
        Long policyId = 999L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.empty());

        assertThrows(CancellationPolicyNotFoundException.class,
                () -> service.update(hotelId, policyId, managerId, requestDto));
    }

    @Test
    void testUpdate_PolicyNotBelongToHotel() {
        Long hotelId = 1L;
        Long policyId = 1L;
        Long managerId = 100L;

        Hotel differentHotel = Hotel.builder()
                .id(2L)
                .name("Different Hotel")
                .managerId(100L)
                .build();

        CancellationPolicy differentPolicy = CancellationPolicy.builder()
                .id(policyId)
                .hotel(differentHotel)
                .name(CancellationPolicyName.MODERATE)
                .build();

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(differentPolicy));

        assertThrows(CancellationPolicyNotBelongToHotelException.class,
                () -> service.update(hotelId, policyId, managerId, requestDto));
    }

    @Test
    void testUpdate_AccessDenied() {
        Long hotelId = 1L;
        Long policyId = 1L;
        Long managerId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(policy));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.update(hotelId, policyId, managerId, requestDto));

            verify(repository, never()).save(any());
        }
    }

    @Test
    void testUpdate_DuplicateName() {
        Long hotelId = 1L;
        Long policyId = 1L;
        Long managerId = 100L;

        CancellationPolicyRequestDto updateDto = new CancellationPolicyRequestDto();
        updateDto.setName(CancellationPolicyName.STRICT);
        updateDto.setDescription("Updated");
        updateDto.setDaysBeforeCheckin(5);
        updateDto.setRefundPercentage(BigDecimal.valueOf(50.0));

        CancellationPolicy existingPolicy = CancellationPolicy.builder()
                .id(2L)
                .hotel(hotel)
                .name(CancellationPolicyName.STRICT)
                .build();

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(repository.findByHotel_IdAndName(hotelId, updateDto.getName()))
                .thenReturn(Optional.of(existingPolicy));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(DuplicateCancellationPolicyException.class,
                    () -> service.update(hotelId, policyId, managerId, updateDto));

            verify(repository, never()).save(any());
        }
    }

    @Test
    void testDelete_Success() {
        Long hotelId = 1L;
        Long policyId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(roomRepository.existsByCancellationPolicyId(policyId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            service.delete(hotelId, policyId, managerId);

            verify(repository, times(1)).delete(any(CancellationPolicy.class));
        }
    }

    @Test
    void testDelete_HotelNotFound() {
        Long hotelId = 999L;
        Long policyId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        assertThrows(HotelNotFoundException.class,
                () -> service.delete(hotelId, policyId, managerId));

        verify(repository, never()).delete(any(CancellationPolicy.class));
    }

    @Test
    void testDelete_PolicyNotFound() {
        Long hotelId = 1L;
        Long policyId = 999L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.empty());

        assertThrows(CancellationPolicyNotFoundException.class,
                () -> service.delete(hotelId, policyId, managerId));

        verify(repository, never()).delete(any(CancellationPolicy.class));
    }

    @Test
    void testDelete_PolicyNotBelongToHotel() {
        Long hotelId = 1L;
        Long policyId = 1L;
        Long managerId = 100L;

        Hotel differentHotel = Hotel.builder()
                .id(2L)
                .name("Different Hotel")
                .managerId(100L)
                .build();

        CancellationPolicy differentPolicy = CancellationPolicy.builder()
                .id(policyId)
                .hotel(differentHotel)
                .name(CancellationPolicyName.MODERATE)
                .build();

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(differentPolicy));

        assertThrows(CancellationPolicyNotBelongToHotelException.class,
                () -> service.delete(hotelId, policyId, managerId));

        verify(repository, never()).delete(any(CancellationPolicy.class));
    }

    @Test
    void testDelete_AccessDenied() {
        Long hotelId = 1L;
        Long policyId = 1L;
        Long managerId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(policy));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.delete(hotelId, policyId, managerId));

            verify(repository, never()).delete(any(CancellationPolicy.class));
        }
    }

    @Test
    void testDelete_PolicyAssignedToRooms() {
        Long hotelId = 1L;
        Long policyId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(roomRepository.existsByCancellationPolicyId(policyId)).thenReturn(true);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(IllegalStateException.class,
                    () -> service.delete(hotelId, policyId, managerId));

            verify(repository, never()).delete(any(CancellationPolicy.class));
        }
    }

    @Test
    void testDelete_AdminAccess() {
        Long hotelId = 1L;
        Long policyId = 1L;
        Long managerId = 999L; // Different manager

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(roomRepository.existsByCancellationPolicyId(policyId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            service.delete(hotelId, policyId, managerId);

            verify(repository, times(1)).delete(any(CancellationPolicy.class));
        }
    }
}
