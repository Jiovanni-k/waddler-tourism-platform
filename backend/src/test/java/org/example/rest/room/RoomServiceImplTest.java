package org.example.rest.room;

import org.example.rest.PagedResponse;
import org.example.rest.amenity.Amenity;
import org.example.rest.amenity.AmenityDto;
import org.example.rest.booking.Booking;
import org.example.rest.booking.BookingRepository;
import org.example.rest.booking.BookingStatus;
import org.example.rest.cancellationpolicy.CancellationPolicyNotBelongToHotelException;
import org.example.rest.cancellationpolicy.CancellationPolicyRepository;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.notification.EmailService;
import org.example.rest.security.SecurityUtil;
import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private CancellationPolicyRepository cancellationPolicyRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RoomServiceImpl service;

    private Hotel hotel;
    private Room room;
    private Amenity amenity;
    private RoomRequestDto requestDto;
    private RoomResponseDto responseDto;

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
        room.setRoomType(RoomType.SINGLE);
        room.setMaxCapacity(2);
        room.setTotalRooms(5);
        room.setBasePrice(BigDecimal.valueOf(100.00));
        room.setBedType("Queen");
        room.setActive(true);
        room.setAmenities(new HashSet<>());

        amenity = new Amenity();
        amenity.setId(1L);
        amenity.setName("WiFi");

        requestDto = new RoomRequestDto();
        requestDto.setName("Room 101");
        requestDto.setRoomType(RoomType.SINGLE);
        requestDto.setMaxCapacity(2);
        requestDto.setTotalRooms(5);
        requestDto.setBasePrice(BigDecimal.valueOf(100.00));
        requestDto.setBedType("Queen");
        requestDto.setActive(true);

        responseDto = new RoomResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Room 101");
        responseDto.setRoomType(RoomType.SINGLE);
    }

    @Test
    void testCreate_Success() {
        Long hotelId = 1L;
        Long managerId = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomRepository.existsByHotel_IdAndName(hotelId, requestDto.getName())).thenReturn(false);
        when(roomMapper.toEntity(eq(requestDto), eq(hotel), any(Set.class))).thenReturn(room);
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            RoomResponseDto result = service.create(hotelId, managerId, requestDto);

            assertNotNull(result);
            verify(roomRepository, times(1)).save(any(Room.class));
        }
    }

    @Test
    void testCreate_HotelNotFound() {
        Long hotelId = 999L;
        Long managerId = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class,
                () -> service.create(hotelId, managerId, requestDto));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void testCreate_DuplicateRoom() {
        Long hotelId = 1L;
        Long managerId = 100L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomRepository.existsByHotel_IdAndName(hotelId, requestDto.getName())).thenReturn(true);

        assertThrows(DuplicateRoomException.class,
                () -> service.create(hotelId, managerId, requestDto));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void testCreate_AccessDenied() {
        Long hotelId = 1L;
        Long differentManagerId = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.create(hotelId, differentManagerId, requestDto));

            verify(roomRepository, never()).save(any());
        }
    }

    @Test
    void testCreate_InvalidCancellationPolicy() {
        Long hotelId = 1L;
        Long managerId = 100L;

        requestDto.setCancellationPolicyId(999L);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomRepository.existsByHotel_IdAndName(hotelId, requestDto.getName())).thenReturn(false);
        when(cancellationPolicyRepository.existsByIdAndHotel_Id(999L, hotelId)).thenReturn(false);

        assertThrows(CancellationPolicyNotBelongToHotelException.class,
                () -> service.create(hotelId, managerId, requestDto));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void testCreate_AdminCanCreateForAnyHotel() {
        Long hotelId = 1L;
        Long adminId = 999L;

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomRepository.existsByHotel_IdAndName(hotelId, requestDto.getName())).thenReturn(false);
        when(roomMapper.toEntity(eq(requestDto), eq(hotel), any(Set.class))).thenReturn(room);
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            RoomResponseDto result = service.create(hotelId, adminId, requestDto);

            assertNotNull(result);
            verify(roomRepository, times(1)).save(any(Room.class));
        }
    }

    @Test
    void testGetById_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        RoomResponseDto result = service.getById(hotelId, roomId);

        assertNotNull(result);
        verify(roomRepository, times(1)).findById(roomId);
    }

    @Test
    void testGetById_HotelNotFound() {
        Long hotelId = 999L;
        Long roomId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        assertThrows(HotelNotFoundException.class,
                () -> service.getById(hotelId, roomId));
    }

    @Test
    void testGetById_RoomNotFound() {
        Long hotelId = 1L;
        Long roomId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class,
                () -> service.getById(hotelId, roomId));
    }

    @Test
    void testGetById_RoomNotBelongToHotel() {
        Long hotelId = 1L;
        Long roomId = 1L;

        room.setHotel(new Hotel());
        room.getHotel().setId(999L);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThrows(RoomNotBelongToHotelException.class,
                () -> service.getById(hotelId, roomId));
    }

    @Test
    void testList_Success() {
        Long hotelId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Room> page = new PageImpl<>(List.of(room), pageable, 1);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        PagedResponse<RoomResponseDto> result = service.list(
                hotelId, null, null, null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testList_WithFilters() {
        Long hotelId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Room> page = new PageImpl<>(List.of(room), pageable, 1);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        PagedResponse<RoomResponseDto> result = service.list(
                hotelId, "Room", RoomType.SINGLE, true, "Queen", 2,
                BigDecimal.valueOf(50), BigDecimal.valueOf(150), null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testList_InvalidPriceRange() {
        Long hotelId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.list(hotelId, null, null, null, null, null,
                        BigDecimal.valueOf(150), BigDecimal.valueOf(50), null, pageable));
    }

    @Test
    void testUpdate_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            when(hotelRepository.existsById(hotelId)).thenReturn(true);
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(roomRepository.save(any(Room.class))).thenReturn(room);
            when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

            RoomResponseDto result = service.update(hotelId, roomId, managerId, requestDto);

            assertNotNull(result);
            verify(roomRepository, times(1)).save(any(Room.class));
        }
    }

    @Test
    void testUpdate_DuplicateNameOtherRoom() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        requestDto.setName("Different Room");

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.existsByHotel_IdAndName(hotelId, requestDto.getName())).thenReturn(true);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(DuplicateRoomException.class,
                    () -> service.update(hotelId, roomId, managerId, requestDto));

            verify(roomRepository, never()).save(any());
        }
    }

    @Test
    void testActivate_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        room.setActive(false);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            RoomResponseDto result = service.activate(hotelId, roomId, managerId);

            assertNotNull(result);
            assertTrue(room.getActive());
            verify(roomRepository, times(1)).save(any(Room.class));
        }
    }

    @Test
    void testActivate_AlreadyActive() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(IllegalStateException.class,
                    () -> service.activate(hotelId, roomId, managerId));

            verify(roomRepository, never()).save(any());
        }
    }

    @Test
    void testDeactivate_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(bookingRepository.findByRoom_Id(roomId)).thenReturn(List.of());
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            RoomResponseDto result = service.deactivate(hotelId, roomId, managerId);

            assertNotNull(result);
            assertFalse(room.getActive());
            verify(roomRepository, times(1)).save(any(Room.class));
        }
    }

    @Test
    void testDeactivate_WithActiveBookings() throws jakarta.mail.MessagingException {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;
        Long userId = 200L;

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUserId(userId);

        AppUser user = new AppUser();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setFirstName("John");

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(bookingRepository.findByRoom_Id(roomId)).thenReturn(List.of(booking));
        when(userRepository.findById(managerId)).thenReturn(Optional.of(user));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toResponseDto(room)).thenReturn(responseDto);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            RoomResponseDto result = service.deactivate(hotelId, roomId, managerId);

            assertNotNull(result);
            verify(bookingRepository, times(1)).save(any(Booking.class));
            verify(emailService, times(1)).sendRoomDeactivated(
                    anyString(), anyString(), any(Booking.class), anyString(), anyString(), anyString());
        }
    }

    @Test
    void testDeactivate_AlreadyInactive() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        room.setActive(false);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(IllegalStateException.class,
                    () -> service.deactivate(hotelId, roomId, managerId));

            verify(roomRepository, never()).save(any());
        }
    }

    @Test
    void testGetRoomAmenities_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;

        room.getAmenities().add(amenity);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        List<AmenityDto> result = service.getRoomAmenities(hotelId, roomId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetRoomAmenities_Empty() {
        Long hotelId = 1L;
        Long roomId = 1L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        List<AmenityDto> result = service.getRoomAmenities(hotelId, roomId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
