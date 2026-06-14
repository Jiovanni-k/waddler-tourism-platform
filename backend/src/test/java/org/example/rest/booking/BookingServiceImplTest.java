package org.example.rest.booking;

import jakarta.mail.MessagingException;
import org.example.rest.inventory.Inventory;
import org.example.rest.inventory.InventoryRepository;
import org.example.rest.notification.EmailService;
import org.example.rest.payment.Payment;
import org.example.rest.payment.PaymentRepository;
import org.example.rest.payment.refund.RefundRepository;
import org.example.rest.pricingrule.PricingRuleService;
import org.example.rest.room.Room;
import org.example.rest.room.RoomRepository;
import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserRepository;
import org.example.rest.hotel.Hotel;
import org.example.rest.cancellationpolicy.CancellationPolicyName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PricingRuleService pricingRuleService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequestDto requestDto;
    private Room room;
    private Booking booking;
    private AppUser user;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        requestDto = new BookingRequestDto(
                1L,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2
        );

        user = new AppUser();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");

        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");

        room = Room.builder()
                .id(1L)
                .name("Room 101")
                .maxCapacity(2)
                .active(true)
                .hotel(hotel)
                .build();

        booking = Booking.builder()
                .id(1L)
                .userId(1L)
                .room(room)
                .numberOfGuests(2)
                .checkInDate(LocalDate.now().plusDays(5))
                .checkOutDate(LocalDate.now().plusDays(10))
                .status(BookingStatus.PENDING)
                .totalPrice(500.0)
                .bookingDate(LocalDateTime.now())
                .build();

        inventory = Inventory.builder()
                .id(1L)
                .room(room)
                .date(LocalDate.now().plusDays(5))
                .totalRooms(10)
                .availableRooms(10)
                .build();
    }

    @Test
    void testCreateBooking_Success() throws MessagingException {
        Long userId = 1L;
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(pricingRuleService.calculateTotalPrice(1L, requestDto.getCheckInDate(), requestDto.getCheckOutDate()))
                .thenReturn(BigDecimal.valueOf(500.0));
        when(inventoryRepository.findByRoomIdAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(inventory));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toEntity(any(BookingRequestDto.class), eq(userId), eq(room)))
                .thenReturn(booking);
        when(bookingMapper.toResponseDto(booking))
                .thenReturn(new BookingResponseDto());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BookingResponseDto result = bookingService.create(requestDto, userId);

        assertNotNull(result);
        verify(roomRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailService, times(1)).sendBookingPendingToUser(anyString(), anyString(), any(Booking.class), anyString());
    }

    @Test
    void testCreateBooking_UserIdNull_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(requestDto, null));
    }

    @Test
    void testCreateBooking_RoomNotFound_ThrowsException() {

        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(requestDto, 1L));
    }

    @Test
    void testCreateBooking_RoomInactive_ThrowsException() {

        room.setActive(false);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.create(requestDto, 1L));
    }

    @Test
    void testCreateBooking_CheckInDateInPast_ThrowsException() {

        requestDto.setCheckInDate(LocalDate.now().minusDays(1));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.create(requestDto, 1L));
    }

    @Test
    void testCreateBooking_CheckOutBeforeCheckIn_ThrowsException() {

        requestDto.setCheckOutDate(LocalDate.now().plusDays(3));
        requestDto.setCheckInDate(LocalDate.now().plusDays(5));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.create(requestDto, 1L));
    }

    @Test
    void testCreateBooking_GuestExceedsCapacity_ThrowsException() {

        requestDto.setNumberOfGuests(5);
        room.setMaxCapacity(2);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.create(requestDto, 1L));
    }

    @Test
    void testCreateBooking_NoInventoryAvailable_ThrowsException() {

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(pricingRuleService.calculateTotalPrice(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(500.0));
        when(inventoryRepository.findByRoomIdAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.create(requestDto, 1L));
    }

    @Test
    void testGetBookingById_AdminAccess_Success() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.getById(1L, 999L, "ADMIN");

        assertNotNull(result);
        verify(bookingRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBookingById_UserAccess_Success() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.getById(1L, 1L, "USER");

        assertNotNull(result);
    }

    @Test
    void testGetBookingById_UserAccessDenied_ThrowsException() {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingAccessDeniedException.class,
                () -> bookingService.getById(1L, 999L, "USER"));
    }

    @Test
    void testGetBookingById_NotFound_ThrowsException() {

        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.getById(999L, 1L, "ADMIN"));
    }

    @Test
    void testConfirmBooking_Success() throws MessagingException {

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BookingResponseDto result = bookingService.confirm(1L, 1L, "USER");

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailService, times(1)).sendBookingConfirmation(anyString(), anyString(), any(Booking.class), anyString());
    }

    @Test
    void testConfirmBooking_AlreadyConfirmed_ThrowsException() {

        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.confirm(1L, 1L, "USER"));
    }

    @Test
    void testConfirmBooking_Cancelled_ThrowsException() {

        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.confirm(1L, 1L, "USER"));
    }

    @Test
    void testCancelBooking_WithinPolicy_Success() {

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCancellationDaysBeforeCheckin(5);
        booking.setCancellationRefundPercentage(BigDecimal.valueOf(100.0));
        booking.setTotalPrice(500.0);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BookingResponseDto result = bookingService.cancel(1L, 1L, "USER");

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testCancelBooking_PastCancellationDeadline_ThrowsException() {

        Booking confirmedBooking = Booking.builder()
                .id(1L)
                .userId(1L)
                .room(room)
                .numberOfGuests(2)
                .checkInDate(LocalDate.now().plusDays(2)) // CheckIn in 2 days
                .checkOutDate(LocalDate.now().plusDays(7))
                .status(BookingStatus.CONFIRMED)
                .totalPrice(500.0)
                .bookingDate(LocalDateTime.now())
                .cancellationPolicyName(CancellationPolicyName.MODERATE)
                .cancellationDaysBeforeCheckin(5) // Requires 5 days notice
                .cancellationRefundPercentage(BigDecimal.valueOf(100.0))
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(confirmedBooking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.cancel(1L, 1L, "USER"));
    }

    @Test
    void testCancelBooking_AlreadyCancelled_ThrowsException() {

        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.cancel(1L, 1L, "USER"));
    }

    @Test
    void testCancelBooking_Completed_ThrowsException() {

        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.cancel(1L, 1L, "USER"));
    }

    @Test
    void testCompleteBooking_Success() {

        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.complete(1L, 1L, "USER");

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testDeleteBooking_Success() {

        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_Id(1L)).thenReturn(List.of());

        bookingService.delete(1L);

        verify(bookingRepository, times(1)).delete(booking);
    }

    @Test
    void testDeleteBooking_WithPayments_DeletesPaymentsAndRefunds() {

        booking.setStatus(BookingStatus.PENDING);
        Payment payment = new Payment();
        payment.setId(1L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_Id(1L)).thenReturn(List.of(payment));
        when(refundRepository.findByPaymentId(1L)).thenReturn(List.of());

        bookingService.delete(1L);

        verify(paymentRepository, times(1)).deleteAll(any());
        verify(bookingRepository, times(1)).delete(booking);
    }

    @Test
    void testUpdateBooking_Success() {

        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(pricingRuleService.calculateTotalPrice(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(600.0));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.update(1L, requestDto, 1L, "USER");

        assertNotNull(result);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testUpdateBooking_CancelledStatus_ThrowsException() {

        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.update(1L, requestDto, 1L, "USER"));
    }
    @Test
    void testListBookings_Success() {

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Booking> page =
                new org.springframework.data.domain.PageImpl<>(List.of(booking), pageable, 1);

        when(bookingRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(page);
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());

        org.example.rest.PagedResponse<BookingResponseDto> result = bookingService.list(
                1L, BookingStatus.PENDING, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testListBookings_WithFilters() {

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Booking> page =
                new org.springframework.data.domain.PageImpl<>(List.of(booking), pageable, 1);

        when(bookingRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(page);
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());

        // Act
        org.example.rest.PagedResponse<BookingResponseDto> result = bookingService.list(
                1L,
                BookingStatus.CONFIRMED,
                1L,              // hotelId
                100.0,           // minPrice
                600.0,           // maxPrice
                CancellationPolicyName.MODERATE,
                pageable);

        assertNotNull(result);
    }

    @Test
    void testListBookings_InvalidPriceRange_ThrowsException() {

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        assertThrows(InvalidPriceRangeException.class,
                () -> bookingService.list(1L, null, null, 600.0, 100.0, null, pageable));
    }

    @Test
    void testListBookings_NegativePrice_ThrowsException() {

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        assertThrows(InvalidPriceRangeException.class,
                () -> bookingService.list(1L, null, null, -100.0, 500.0, null, pageable));
    }

    @Test
    void testListByHotelIds_Success() {

        List<Long> hotelIds = List.of(1L, 2L);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Booking> page =
                new org.springframework.data.domain.PageImpl<>(List.of(booking), pageable, 1);

        when(bookingRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(page);
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());

        org.example.rest.PagedResponse<BookingResponseDto> result = bookingService.listByHotelIds(
                hotelIds, BookingStatus.CONFIRMED, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetBookingById_HotelManagerAccess_Success() {

        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setManagerId(1L); // manager id = current user
        room.setHotel(hotel);
        booking.setRoom(room);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.getById(1L, 1L, "HOTEL_MANAGER");

        assertNotNull(result);
    }

    @Test
    void testGetBookingById_HotelManagerAccessDenied_ThrowsException() {

        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setManagerId(999L); // Different manager
        room.setHotel(hotel);
        booking.setRoom(room);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingAccessDeniedException.class,
                () -> bookingService.getById(1L, 1L, "HOTEL_MANAGER"));
    }

    @Test
    void testConfirmBooking_HotelManagerAccess_Success() throws MessagingException {

        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setManagerId(1L);
        room.setHotel(hotel);
        booking.setRoom(room);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BookingResponseDto result = bookingService.confirm(1L, 1L, "HOTEL_MANAGER");

        assertNotNull(result);
    }

    @Test
    void testCompleteBooking_NotConfirmed_ThrowsException() {

        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.complete(1L, 1L, "USER"));
    }

    @Test
    void testDeleteBooking_NotFound_ThrowsException() {

        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.delete(999L));
    }

    @Test
    void testUpdateBooking_NotFound_ThrowsException() {

        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.update(999L, requestDto, 1L, "USER"));
    }

    @Test
    void testUpdateBooking_CompletedStatus_ThrowsException() {

        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingForbiddenActionException.class,
                () -> bookingService.update(1L, requestDto, 1L, "USER"));
    }
}