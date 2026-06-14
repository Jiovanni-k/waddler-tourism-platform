package org.example.rest.pricingrule;

import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.room.Room;
import org.example.rest.room.RoomNotFoundException;
import org.example.rest.room.RoomNotBelongToHotelException;
import org.example.rest.room.RoomRepository;
import org.example.rest.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingRuleServiceImplTest {

    @Mock
    private PricingRuleRepository repository;

    @Mock
    private PricingRuleMapper mapper;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private PricingRuleServiceImpl service;

    private Hotel hotel;
    private Room room;
    private PricingRule pricingRule;
    private PricingRuleRequestDto requestDto;
    private PricingRuleResponseDto responseDto;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setManagerId(100L);

        room = new Room();
        room.setId(1L);
        room.setHotel(hotel);
        room.setName("Room 101");
        room.setBasePrice(BigDecimal.valueOf(100.00));

        pricingRule = new PricingRule();
        pricingRule.setId(1L);
        pricingRule.setRoom(room);
        pricingRule.setName("Weekend Rate");
        pricingRule.setDescription("Higher rate for weekends");
        pricingRule.setStartDate(LocalDate.now().plusDays(1));
        pricingRule.setEndDate(LocalDate.now().plusDays(30));
        pricingRule.setPricePerNight(BigDecimal.valueOf(150.00));
        pricingRule.setPriority(1);
        pricingRule.setActive(true);

        requestDto = new PricingRuleRequestDto();
        requestDto.setName("Weekend Rate");
        requestDto.setDescription("Higher rate for weekends");
        requestDto.setStartDate(LocalDate.now().plusDays(1));
        requestDto.setEndDate(LocalDate.now().plusDays(30));
        requestDto.setPricePerNight(BigDecimal.valueOf(150.00));
        requestDto.setPriority(1);
        requestDto.setActive(true);

        responseDto = new PricingRuleResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Weekend Rate");
        responseDto.setPricePerNight(BigDecimal.valueOf(150.00));
    }

    @Test
    void testCreate_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.existsByRoom_IdAndName(roomId, requestDto.getName())).thenReturn(false);
        when(mapper.toEntity(requestDto, room)).thenReturn(pricingRule);
        when(repository.save(any(PricingRule.class))).thenReturn(pricingRule);
        when(mapper.toDto(pricingRule)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            PricingRuleResponseDto result = service.create(hotelId, roomId, managerId, requestDto);

            assertNotNull(result);
            verify(repository, times(1)).save(any(PricingRule.class));
        }
    }

    @Test
    void testCreate_HotelNotFound() {
        Long hotelId = 999L;
        Long roomId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        assertThrows(HotelNotFoundException.class,
                () -> service.create(hotelId, roomId, managerId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_RoomNotFound() {
        Long hotelId = 1L;
        Long roomId = 999L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class,
                () -> service.create(hotelId, roomId, managerId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_RoomNotBelongToHotel() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        room.setHotel(new Hotel());
        room.getHotel().setId(999L);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThrows(RoomNotBelongToHotelException.class,
                () -> service.create(hotelId, roomId, managerId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_InvalidDateRange() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        requestDto.setEndDate(LocalDate.now());
        requestDto.setStartDate(LocalDate.now().plusDays(10));

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(hotelId, roomId, managerId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_DuplicateRule() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.existsByRoom_IdAndName(roomId, requestDto.getName())).thenReturn(true);

        assertThrows(DuplicatePricingRuleException.class,
                () -> service.create(hotelId, roomId, managerId, requestDto));

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_AccessDenied() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long differentManagerId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.create(hotelId, roomId, differentManagerId, requestDto));

            verify(repository, never()).save(any());
        }
    }

    @Test
    void testCreate_AdminCanCreateForAnyHotel() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long adminId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.existsByRoom_IdAndName(roomId, requestDto.getName())).thenReturn(false);
        when(mapper.toEntity(requestDto, room)).thenReturn(pricingRule);
        when(repository.save(any(PricingRule.class))).thenReturn(pricingRule);
        when(mapper.toDto(pricingRule)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            PricingRuleResponseDto result = service.create(hotelId, roomId, adminId, requestDto);

            assertNotNull(result);
            verify(repository, times(1)).save(any(PricingRule.class));
        }
    }

    @Test
    void testGetById_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long ruleId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findById(ruleId)).thenReturn(Optional.of(pricingRule));
        when(mapper.toDto(pricingRule)).thenReturn(responseDto);

        PricingRuleResponseDto result = service.getById(hotelId, roomId, ruleId);

        assertNotNull(result);
        verify(repository, times(1)).findById(ruleId);
    }

    @Test
    void testGetById_RuleNotFound() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long ruleId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findById(ruleId)).thenReturn(Optional.empty());

        assertThrows(PricingRuleNotFoundException.class,
                () -> service.getById(hotelId, roomId, ruleId));
    }

    @Test
    void testListByRoom_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findByRoom_Id(roomId)).thenReturn(List.of(pricingRule));
        when(mapper.toDto(pricingRule)).thenReturn(responseDto);

        List<PricingRuleResponseDto> result = service.listByRoom(hotelId, roomId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByRoom_Id(roomId);
    }

    @Test
    void testListByRoom_Empty() {
        Long hotelId = 1L;
        Long roomId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findByRoom_Id(roomId)).thenReturn(List.of());

        List<PricingRuleResponseDto> result = service.listByRoom(hotelId, roomId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long ruleId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findById(ruleId)).thenReturn(Optional.of(pricingRule));
        when(repository.save(any(PricingRule.class))).thenReturn(pricingRule);
        when(mapper.toDto(pricingRule)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            PricingRuleResponseDto result = service.update(hotelId, roomId, ruleId, managerId, requestDto);

            assertNotNull(result);
            verify(repository, times(1)).save(any(PricingRule.class));
        }
    }

    @Test
    void testUpdate_InvalidDateRange() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long ruleId = 1L;
        Long managerId = 100L;

        requestDto.setEndDate(LocalDate.now());
        requestDto.setStartDate(LocalDate.now().plusDays(10));

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findById(ruleId)).thenReturn(Optional.of(pricingRule));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(IllegalArgumentException.class,
                    () -> service.update(hotelId, roomId, ruleId, managerId, requestDto));

            verify(repository, never()).save(any());
        }
    }

    @Test
    void testUpdate_DuplicateNameOtherRule() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long ruleId = 1L;
        Long managerId = 100L;

        requestDto.setName("Different Name");

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findById(ruleId)).thenReturn(Optional.of(pricingRule));
        when(repository.existsByRoom_IdAndNameAndIdNot(roomId, requestDto.getName(), ruleId))
                .thenReturn(true);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(DuplicatePricingRuleException.class,
                    () -> service.update(hotelId, roomId, ruleId, managerId, requestDto));

            verify(repository, never()).save(any());
        }
    }

    @Test
    void testDelete_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long ruleId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findById(ruleId)).thenReturn(Optional.of(pricingRule));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            service.delete(hotelId, roomId, ruleId, managerId);

            verify(repository, times(1)).delete(any(PricingRule.class));
        }
    }

    @Test
    void testDelete_AccessDenied() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long ruleId = 1L;
        Long differentManagerId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.delete(hotelId, roomId, ruleId, differentManagerId));

            verify(repository, never()).delete(any());
        }
    }

    @Test
    void testCalculateTotalPrice_Success() {
        Long roomId = 1L;
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(4); // 3 nights

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findActiveRulesForDateRange(roomId, checkIn, checkOut))
                .thenReturn(List.of(pricingRule));

        BigDecimal result = service.calculateTotalPrice(roomId, checkIn, checkOut);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(450.00), result); // 3 nights * 150
        verify(repository, times(1)).findActiveRulesForDateRange(roomId, checkIn, checkOut);
    }

    @Test
    void testCalculateTotalPrice_NoRulesApply() {
        Long roomId = 1L;
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(4); // 3 nights

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findActiveRulesForDateRange(roomId, checkIn, checkOut))
                .thenReturn(List.of());

        BigDecimal result = service.calculateTotalPrice(roomId, checkIn, checkOut);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(300.00), result); // 3 nights * 100 (base price)
    }

    @Test
    void testCalculateTotalPrice_InvalidDates() {
        Long roomId = 1L;
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateTotalPrice(roomId, checkIn, checkOut));

        verify(repository, never()).findActiveRulesForDateRange(any(), any(), any());
    }

    @Test
    void testCalculateTotalPrice_NullDates() {
        Long roomId = 1L;

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateTotalPrice(roomId, null, null));

        verify(repository, never()).findActiveRulesForDateRange(any(), any(), any());
    }

    @Test
    void testCalculateTotalPrice_RoomNotFound() {
        Long roomId = 999L;
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(4);

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class,
                () -> service.calculateTotalPrice(roomId, checkIn, checkOut));
    }

    @Test
    void testCalculateTotalPrice_MultipleRules() {
        Long roomId = 1L;
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(4); // 3 nights

        PricingRule rule2 = new PricingRule();
        rule2.setPriority(2);
        rule2.setPricePerNight(BigDecimal.valueOf(120.00));

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(repository.findActiveRulesForDateRange(roomId, checkIn, checkOut))
                .thenReturn(List.of(pricingRule, rule2));

        BigDecimal result = service.calculateTotalPrice(roomId, checkIn, checkOut);

        assertNotNull(result);
        // Should use first matching rule (highest priority)
        assertEquals(BigDecimal.valueOf(450.00), result); // 3 nights * 150
    }
}
