package org.example.rest.payment;

import org.example.rest.PagedResponse;
import org.example.rest.booking.Booking;
import org.example.rest.booking.BookingNotFoundException;
import org.example.rest.booking.BookingRepository;
import org.example.rest.booking.BookingStatus;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.room.Room;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private PaymentServiceImpl service;

    private Hotel hotel;
    private Room room;
    private Booking booking;
    private Payment payment;
    private PaymentRequestDto requestDto;
    private PaymentResponseDto responseDto;

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

        booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);
        booking.setUserId(200L);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(100.0);

        payment = new Payment();
        payment.setId(1L);
        payment.setBooking(booking);
        payment.setUserId(200L);
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setPaymentCode("PAY-2026-000001");
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);

        requestDto = new PaymentRequestDto();
        requestDto.setBookingId(1L);
        requestDto.setAmount(BigDecimal.valueOf(100.00));
        requestDto.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        responseDto = new PaymentResponseDto();
        responseDto.setId(1L);
        responseDto.setPaymentCode("PAY-2026-000001");
        responseDto.setAmount(BigDecimal.valueOf(100.00));
        responseDto.setPaymentStatus(PaymentStatus.SUCCEEDED);
    }

    @Test
    void testCreatePayment_Success() {
        Long userId = 200L;

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(paymentRepository.existsByBooking_IdAndPaymentStatusIn(1L,
                    List.of(PaymentStatus.SUCCEEDED, PaymentStatus.PROCESSING)))
                    .thenReturn(false);
            when(paymentMapper.toEntity(any(PaymentRequestDto.class), eq(userId), anyString(), eq(booking)))
                    .thenReturn(payment);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                Payment p = invocation.getArgument(0);
                // if status is null, set it to SUCCEEDED
                if (p.getPaymentStatus() == null) {
                    p.setPaymentStatus(PaymentStatus.SUCCEEDED);
                    p.setProcessedAt(LocalDateTime.now());
                }
                return p;
            });
            when(paymentMapper.toDto(payment)).thenReturn(responseDto);

            PaymentResponseDto result = service.createPayment(requestDto);

            assertNotNull(result);
            verify(paymentRepository, atLeast(2)).save(any(Payment.class));
            verify(bookingRepository, times(1)).save(any(Booking.class));
        }
    }

    @Test
    void testCreatePayment_UserNotAuthenticated() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            assertThrows(IllegalArgumentException.class,
                    () -> service.createPayment(requestDto));

            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    void testCreatePayment_BookingNotFound() {
        Long userId = 200L;

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(BookingNotFoundException.class,
                    () -> service.createPayment(requestDto));

            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    void testCreatePayment_UserNotOwner() {
        Long userId = 200L;
        Long differentUserId = 999L;

        booking.setUserId(differentUserId);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            assertThrows(AccessDeniedException.class,
                    () -> service.createPayment(requestDto));

            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    void testCreatePayment_BookingCancelled() {
        Long userId = 200L;

        booking.setStatus(BookingStatus.CANCELLED);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            assertThrows(PaymentForbiddenActionException.class,
                    () -> service.createPayment(requestDto));

            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    void testCreatePayment_AlreadyPaid() {
        Long userId = 200L;

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(paymentRepository.existsByBooking_IdAndPaymentStatusIn(1L,
                    List.of(PaymentStatus.SUCCEEDED, PaymentStatus.PROCESSING)))
                    .thenReturn(true);

            assertThrows(DuplicatePaymentException.class,
                    () -> service.createPayment(requestDto));

            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    void testCreatePayment_AmountMismatch() {
        Long userId = 200L;

        requestDto.setAmount(BigDecimal.valueOf(50.00));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            assertThrows(PaymentForbiddenActionException.class,
                    () -> service.createPayment(requestDto));

            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    void testCreatePayment_SplitPayment_InvalidFormat() {
        Long userId = 200L;

        requestDto.setPaymentMethod(PaymentMethod.SPLIT);
        requestDto.setSplitBetweenUsers("invalid json");

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(paymentRepository.existsByBooking_IdAndPaymentStatusIn(1L,
                    List.of(PaymentStatus.SUCCEEDED, PaymentStatus.PROCESSING)))
                    .thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> service.createPayment(requestDto));

            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    void testCreatePayment_SplitPayment_Success() {
        Long userId = 200L;

        requestDto.setPaymentMethod(PaymentMethod.SPLIT);
        requestDto.setSplitBetweenUsers("[{\"userId\": 200, \"amount\": 50.00}, {\"userId\": 201, \"amount\": 50.00}]");

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(paymentRepository.existsByBooking_IdAndPaymentStatusIn(1L,
                    List.of(PaymentStatus.SUCCEEDED, PaymentStatus.PROCESSING)))
                    .thenReturn(false);
            when(paymentMapper.toEntity(any(PaymentRequestDto.class), eq(userId), anyString(), eq(booking)))
                    .thenReturn(payment);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                Payment p = invocation.getArgument(0);
                if (p.getPaymentStatus() == null) {
                    p.setPaymentStatus(PaymentStatus.SUCCEEDED);
                    p.setProcessedAt(LocalDateTime.now());
                }
                return p;
            });
            when(paymentMapper.toDto(payment)).thenReturn(responseDto);

            PaymentResponseDto result = service.createPayment(requestDto);

            assertNotNull(result);
            verify(paymentRepository, atLeast(2)).save(any(Payment.class));
            verify(bookingRepository, times(1)).save(any(Booking.class));
        }
    }

    @Test
    void testGetById_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = service.getById(1L);

        assertNotNull(result);
        assertEquals(responseDto, result);
    }

    @Test
    void testGetById_NotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class,
                () -> service.getById(999L));
    }

    @Test
    void testGetByCode_Success() {
        when(paymentRepository.findByPaymentCode("PAY-2026-000001"))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = service.getByCode("PAY-2026-000001");

        assertNotNull(result);
        assertEquals(responseDto, result);
    }

    @Test
    void testList_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> page = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PagedResponse<PaymentResponseDto> result = service.list(
                null, null, null, null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testList_InvalidDateRange() {
        LocalDateTime from = LocalDateTime.now().plusHours(2);
        LocalDateTime to = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.list(null, null, null, null, null, from, to, null, null, pageable));
    }

    @Test
    void testGetUserPaymentHistory_Success() {
        Long userId = 200L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> page = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.findByUserId(userId, pageable)).thenReturn(page);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PagedResponse<PaymentResponseDto> result = service.getUserPaymentHistory(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFlagForFraudDetection_ByAdmin() {
        Long paymentId = 1L;
        Long adminId = 300L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("ADMIN");

            service.flagForFraudDetection(paymentId, adminId, "ADMIN");

            assertTrue(payment.getFraudDetectionFlag());
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Test
    void testFlagForFraudDetection_ManagerAccessDenied() {
        Long paymentId = 1L;
        Long managerId = 999L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(hotelRepository.findByManagerId(managerId)).thenReturn(List.of());

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("HOTEL_MANAGER");

            assertThrows(AccessDeniedException.class,
                    () -> service.flagForFraudDetection(paymentId, managerId, "HOTEL_MANAGER"));

            verify(paymentRepository, never()).save(any());
        }
    }

    @Test
    void testGetFlaggedPayments_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        payment.setFraudDetectionFlag(true);
        Page<Payment> page = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PagedResponse<PaymentResponseDto> result = service.getFlaggedPayments(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
