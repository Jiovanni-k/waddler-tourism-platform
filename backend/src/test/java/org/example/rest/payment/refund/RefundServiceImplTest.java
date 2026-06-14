package org.example.rest.payment.refund;

import org.example.rest.PagedResponse;
import org.example.rest.booking.Booking;
import org.example.rest.booking.BookingStatus;
import org.example.rest.cancellationpolicy.CancellationPolicyName;
import org.example.rest.payment.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceImplTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundMapper refundMapper;

    @InjectMocks
    private RefundServiceImpl service;

    private Booking booking;
    private Payment payment;
    private Refund refund;
    private RefundRequestDto requestDto;
    private RefundResponseDto responseDto;

    @BeforeEach
    void setUp() {
        booking = new Booking();
        booking.setId(1L);
        booking.setUserId(200L);
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setTotalPrice(100.0);
        booking.setCheckInDate(LocalDate.now().plusDays(10));
        booking.setCancellationPolicyName(CancellationPolicyName.FLEXIBLE);
        booking.setCancellationDaysBeforeCheckin(5);
        booking.setCancellationRefundPercentage(BigDecimal.valueOf(75));

        payment = new Payment();
        payment.setId(1L);
        payment.setBooking(booking);
        payment.setUserId(200L);
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setPaymentCode("PAY-2026-000001");
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);

        refund = new Refund();
        refund.setId(1L);
        refund.setPayment(payment);
        refund.setAmount(BigDecimal.valueOf(75.00));
        refund.setRefundCode("REF-2026-000001");
        refund.setStatus(RefundStatus.COMPLETED);
        refund.setProcessedAt(LocalDateTime.now());

        requestDto = new RefundRequestDto();
        requestDto.setPaymentId(1L);

        responseDto = new RefundResponseDto();
        responseDto.setId(1L);
        responseDto.setRefundCode("REF-2026-000001");
        responseDto.setAmount(BigDecimal.valueOf(75.00));
        responseDto.setStatus(RefundStatus.COMPLETED);
    }

    @Test
    void testCreateRefund_Success() {
        Long userId = 200L;

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(refundRepository.findByPaymentId(1L)).thenReturn(List.of());
            when(refundMapper.toEntity(any(RefundRequestDto.class), eq(payment), anyString(), any(BigDecimal.class)))
                    .thenReturn(refund);
            when(refundRepository.save(any(Refund.class))).thenReturn(refund);
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
            when(refundMapper.toDto(refund)).thenReturn(responseDto);

            RefundResponseDto result = service.createRefund(requestDto);

            assertNotNull(result);
            verify(refundRepository, times(1)).save(any(Refund.class));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Test
    void testCreateRefund_UserNotAuthenticated() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            assertThrows(IllegalArgumentException.class,
                    () -> service.createRefund(requestDto));

            verify(refundRepository, never()).save(any());
        }
    }

    @Test
    void testCreateRefund_PaymentNotFound() {
        Long userId = 200L;

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(PaymentNotFoundException.class,
                    () -> service.createRefund(requestDto));

            verify(refundRepository, never()).save(any());
        }
    }

    @Test
    void testCreateRefund_UserNotOwner() {
        Long userId = 200L;
        Long differentUserId = 999L;

        payment.setUserId(differentUserId);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThrows(AccessDeniedException.class,
                    () -> service.createRefund(requestDto));

            verify(refundRepository, never()).save(any());
        }
    }

    @Test
    void testCreateRefund_PaymentNotSucceeded() {
        Long userId = 200L;

        payment.setPaymentStatus(PaymentStatus.FAILED);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThrows(RefundNotAllowedException.class,
                    () -> service.createRefund(requestDto));

            verify(refundRepository, never()).save(any());
        }
    }

    @Test
    void testCreateRefund_BookingNotCancelled() {
        Long userId = 200L;

        booking.setStatus(BookingStatus.CONFIRMED);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThrows(RefundNotAllowedException.class,
                    () -> service.createRefund(requestDto));

            verify(refundRepository, never()).save(any());
        }
    }

    @Test
    void testCreateRefund_CancellationDeadlinePassed() {
        Long userId = 200L;

        booking.setCheckInDate(LocalDate.now().minusDays(1)); // Already checked in

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThrows(RefundNotAllowedException.class,
                    () -> service.createRefund(requestDto));

            verify(refundRepository, never()).save(any());
        }
    }

    @Test
    void testCreateRefund_NoRefundableAmount() {
        Long userId = 200L;

        // already fully refunded
        Refund previousRefund = new Refund();
        previousRefund.setAmount(BigDecimal.valueOf(100.00));
        previousRefund.setStatus(RefundStatus.COMPLETED);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(refundRepository.findByPaymentId(1L)).thenReturn(List.of(previousRefund));

            assertThrows(RefundNotAllowedException.class,
                    () -> service.createRefund(requestDto));

            verify(refundRepository, never()).save(any());
        }
    }

    @Test
    void testCreateRefund_PartiallyRefunded() {
        Long userId = 200L;

        // already partially refunded (50/100)
        Refund previousRefund = new Refund();
        previousRefund.setAmount(BigDecimal.valueOf(50.00));
        previousRefund.setStatus(RefundStatus.COMPLETED);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            securityUtil.when(SecurityUtil::getCurrentUserRole).thenReturn("USER");

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(refundRepository.findByPaymentId(1L)).thenReturn(List.of(previousRefund));
            when(refundMapper.toEntity(any(RefundRequestDto.class), eq(payment), anyString(), any(BigDecimal.class)))
                    .thenReturn(refund);
            when(refundRepository.save(any(Refund.class))).thenReturn(refund);
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
            when(refundMapper.toDto(refund)).thenReturn(responseDto);

            RefundResponseDto result = service.createRefund(requestDto);

            assertNotNull(result);
            // refund should be for remaining 50 (100 - 50 already refunded)
            verify(refundRepository, times(1)).save(any(Refund.class));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Test
    void testCreateRefund_NoPolicyButCancelled() {
        Long userId = 200L;

        // booking has no cancellation policy set
        booking.setCancellationPolicyName(null);
        booking.setCancellationDaysBeforeCheckin(null);
        booking.setCancellationRefundPercentage(null);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThrows(RefundNotAllowedException.class,
                    () -> service.createRefund(requestDto));

            verify(refundRepository, never()).save(any());
        }
    }

    @Test
    void testGetById_Success() {
        when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
        when(refundMapper.toDto(refund)).thenReturn(responseDto);

        RefundResponseDto result = service.getById(1L);

        assertNotNull(result);
        assertEquals(responseDto, result);
    }

    @Test
    void testGetById_NotFound() {
        when(refundRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RefundNotFoundException.class,
                () -> service.getById(999L));
    }

    @Test
    void testGetByCode_Success() {
        when(refundRepository.findByRefundCode("REF-2026-000001"))
                .thenReturn(Optional.of(refund));
        when(refundMapper.toDto(refund)).thenReturn(responseDto);

        RefundResponseDto result = service.getByCode("REF-2026-000001");

        assertNotNull(result);
        assertEquals(responseDto, result);
    }

    @Test
    void testList_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Refund> page = new PageImpl<>(List.of(refund), pageable, 1);

        when(refundRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(refundMapper.toDto(refund)).thenReturn(responseDto);

        PagedResponse<RefundResponseDto> result = service.list(
                null, null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testList_InvalidDateRange() {
        LocalDateTime from = LocalDateTime.now().plusHours(2);
        LocalDateTime to = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> service.list(null, null, null, from, to, null, null, pageable));
    }

    @Test
    void testListByHotelIds_Success() {
        List<Long> hotelIds = List.of(1L);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Refund> page = new PageImpl<>(List.of(refund), pageable, 1);

        when(refundRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(refundMapper.toDto(refund)).thenReturn(responseDto);

        PagedResponse<RefundResponseDto> result = service.listByHotelIds(
                hotelIds, null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetRefundsByPayment_Success() {
        Long paymentId = 1L;
        Long userId = 200L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(refundRepository.findByPaymentId(paymentId)).thenReturn(List.of(refund));
        when(refundMapper.toDto(refund)).thenReturn(responseDto);

        List<RefundResponseDto> result = service.getRefundsByPayment(paymentId, userId, "USER");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetRefundsByPayment_UserAccessDenied() {
        Long paymentId = 1L;
        Long userId = 200L;
        Long differentUserId = 999L;

        payment.setUserId(differentUserId);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(AccessDeniedException.class,
                () -> service.getRefundsByPayment(paymentId, userId, "USER"));
    }

    @Test
    void testGetRefundsByPayment_AdminCanView() {
        Long paymentId = 1L;
        Long adminId = 300L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(refundRepository.findByPaymentId(paymentId)).thenReturn(List.of(refund));
        when(refundMapper.toDto(refund)).thenReturn(responseDto);

        List<RefundResponseDto> result = service.getRefundsByPayment(paymentId, adminId, "ADMIN");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
