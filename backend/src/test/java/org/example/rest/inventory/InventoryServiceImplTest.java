package org.example.rest.inventory;

import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.room.Room;
import org.example.rest.room.RoomNotFoundException;
import org.example.rest.room.RoomNotBelongToHotelException;
import org.example.rest.room.RoomRepository;
import org.example.rest.security.SecurityUtil;
import org.example.rest.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private InventoryServiceImpl service;

    private Hotel hotel;
    private Room room;
    private Inventory inventory;
    private InventoryRequestDto requestDto;

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

        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setRoom(room);
        inventory.setDate(LocalDate.now().plusDays(1));
        inventory.setTotalRooms(10);
        inventory.setAvailableRooms(8);

        requestDto = new InventoryRequestDto();
        requestDto.setDate(LocalDate.now().plusDays(1));
        requestDto.setTotalRooms(10);
        requestDto.setAvailableRooms(8);
    }

    @Test
    void testCreate_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(inventoryRepository.existsByRoomIdAndDate(roomId, requestDto.getDate()))
                .thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            InventoryResponseDto result = service.create(hotelId, roomId, managerId, requestDto);

            assertNotNull(result);
            verify(inventoryRepository, times(1)).save(any(Inventory.class));
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

        verify(inventoryRepository, never()).save(any());
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

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testCreate_RoomNotBelongToHotel() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        room.setHotel(new Hotel()); // Different hotel
        room.getHotel().setId(999L);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThrows(RoomNotBelongToHotelException.class,
                () -> service.create(hotelId, roomId, managerId, requestDto));

        verify(inventoryRepository, never()).save(any());
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

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Test
    void testCreate_InventoryAlreadyExists() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(inventoryRepository.existsByRoomIdAndDate(roomId, requestDto.getDate()))
                .thenReturn(true);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(IllegalArgumentException.class,
                    () -> service.create(hotelId, roomId, managerId, requestDto));

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Test
    void testCreate_AvailableExceedsTotal() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long managerId = 100L;

        requestDto.setAvailableRooms(15);
        requestDto.setTotalRooms(10);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(inventoryRepository.existsByRoomIdAndDate(roomId, requestDto.getDate()))
                .thenReturn(false);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(IllegalArgumentException.class,
                    () -> service.create(hotelId, roomId, managerId, requestDto));

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Test
    void testCreate_AdminCanCreateForAnyHotel() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long adminId = 999L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(inventoryRepository.existsByRoomIdAndDate(roomId, requestDto.getDate()))
                .thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            InventoryResponseDto result = service.create(hotelId, roomId, adminId, requestDto);

            assertNotNull(result);
            verify(inventoryRepository, times(1)).save(any(Inventory.class));
        }
    }

    @Test
    void testGetById_Success() {
        Long inventoryId = 1L;

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));

        InventoryResponseDto result = service.getById(inventoryId);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).findById(inventoryId);
    }

    @Test
    void testGetById_NotFound() {
        Long inventoryId = 999L;

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> service.getById(inventoryId));
    }

    @Test
    void testGetByRoomAndDate_Success() {
        Long roomId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);

        when(inventoryRepository.findByRoomIdAndDate(roomId, date))
                .thenReturn(Optional.of(inventory));

        InventoryResponseDto result = service.getByRoomAndDate(roomId, date);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).findByRoomIdAndDate(roomId, date);
    }

    @Test
    void testGetByRoomAndDate_NotFound() {
        Long roomId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);

        when(inventoryRepository.findByRoomIdAndDate(roomId, date))
                .thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> service.getByRoomAndDate(roomId, date));
    }

    @Test
    void testGetByRoom_Success() {
        Long roomId = 1L;

        when(inventoryRepository.findByRoomId(roomId)).thenReturn(List.of(inventory));

        List<InventoryResponseDto> result = service.getByRoom(roomId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryRepository, times(1)).findByRoomId(roomId);
    }

    @Test
    void testGetByRoom_Empty() {
        Long roomId = 1L;

        when(inventoryRepository.findByRoomId(roomId)).thenReturn(List.of());

        List<InventoryResponseDto> result = service.getByRoom(roomId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByRoomAndDateRange_Success() {
        Long roomId = 1L;
        LocalDate dateFrom = LocalDate.now();
        LocalDate dateTo = LocalDate.now().plusDays(7);

        when(inventoryRepository.findByRoomIdAndDateBetween(roomId, dateFrom, dateTo))
                .thenReturn(List.of(inventory));

        List<InventoryResponseDto> result = service.getByRoomAndDateRange(roomId, dateFrom, dateTo);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetByRoomAndDateRange_InvalidDateRange() {
        Long roomId = 1L;
        LocalDate dateFrom = LocalDate.now().plusDays(7);
        LocalDate dateTo = LocalDate.now();

        assertThrows(IllegalArgumentException.class,
                () -> service.getByRoomAndDateRange(roomId, dateFrom, dateTo));

        verify(inventoryRepository, never()).findByRoomIdAndDateBetween(any(), any(), any());
    }

    @Test
    void testList_Success() {
        Long roomId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(inventoryRepository.findByRoomId(roomId)).thenReturn(List.of(inventory));

        PagedResponse<InventoryResponseDto> result = service.list(roomId, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testList_WithDateRange() {
        Long roomId = 1L;
        LocalDate dateFrom = LocalDate.now();
        LocalDate dateTo = LocalDate.now().plusDays(7);
        Pageable pageable = PageRequest.of(0, 10);

        when(inventoryRepository.findByRoomIdAndDateBetween(roomId, dateFrom, dateTo))
                .thenReturn(List.of(inventory));

        PagedResponse<InventoryResponseDto> result = service.list(roomId, dateFrom, dateTo, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testList_InvalidDateRange() {
        Long roomId = 1L;
        LocalDate dateFrom = LocalDate.now().plusDays(7);
        LocalDate dateTo = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.list(roomId, dateFrom, dateTo, pageable));
    }

    @Test
    void testUpdate_Success() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long inventoryId = 1L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            InventoryResponseDto result = service.update(hotelId, roomId, inventoryId, managerId, requestDto);

            assertNotNull(result);
            verify(inventoryRepository, times(1)).save(any(Inventory.class));
        }
    }

    @Test
    void testUpdate_InventoryNotFound() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long inventoryId = 999L;
        Long managerId = 100L;

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(InventoryNotFoundException.class,
                    () -> service.update(hotelId, roomId, inventoryId, managerId, requestDto));

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Test
    void testUpdate_AvailableExceedsTotal() {
        Long hotelId = 1L;
        Long roomId = 1L;
        Long inventoryId = 1L;
        Long managerId = 100L;

        requestDto.setAvailableRooms(15);
        requestDto.setTotalRooms(10);

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(IllegalArgumentException.class,
                    () -> service.update(hotelId, roomId, inventoryId, managerId, requestDto));

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Test
    void testReserve_Success() {
        Long roomId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        int reserveCount = 2;

        when(inventoryRepository.findByRoomIdAndDate(roomId, date))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryResponseDto result = service.reserve(roomId, date, reserveCount);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testReserve_InvalidCount() {
        Long roomId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        int invalidCount = 0;

        assertThrows(IllegalArgumentException.class,
                () -> service.reserve(roomId, date, invalidCount));

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testReserve_NotFound() {
        Long roomId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        int reserveCount = 2;

        when(inventoryRepository.findByRoomIdAndDate(roomId, date))
                .thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> service.reserve(roomId, date, reserveCount));
    }

    @Test
    void testRelease_Success() {
        Long roomId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        int releaseCount = 2;

        when(inventoryRepository.findByRoomIdAndDate(roomId, date))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryResponseDto result = service.release(roomId, date, releaseCount);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testRelease_InvalidCount() {
        Long roomId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        int invalidCount = -1;

        assertThrows(IllegalArgumentException.class,
                () -> service.release(roomId, date, invalidCount));

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testDelete_Success() {
        Long inventoryId = 1L;

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));

        service.delete(inventoryId);

        verify(inventoryRepository, times(1)).delete(any(Inventory.class));
    }

    @Test
    void testDelete_NotFound() {
        Long inventoryId = 999L;

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> service.delete(inventoryId));

        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    //commiting to trigger pipeline
}
