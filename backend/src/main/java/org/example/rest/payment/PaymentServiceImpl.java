package org.example.rest.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.booking.Booking;
import org.example.rest.booking.BookingNotFoundException;
import org.example.rest.booking.BookingRepository;
import org.example.rest.booking.BookingStatus;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

// Decorator Pattern — CorePaymentService (the Component).
// This is the innermost layer. It only handles the core payment logic:
// resolving the user, checking booking status, building the payment entity,
// and running the mock gateway.
//
// BEFORE: This class also contained duplicate checking, amount validation,
//         split validation, and fraud flagging — all mixed together in
//         one createPayment() method.
// AFTER:  Each of those concerns lives in its own decorator. This class
//         does one job only.
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final Random RANDOM = new Random();
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null)
            throw new IllegalArgumentException("Could not resolve authenticated user");
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));
        String role = SecurityUtil.getCurrentUserRole();
        if ("USER".equals(role) && !booking.getUserId().equals(userId))
            throw new AccessDeniedException("You can only pay for your own bookings");
        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new PaymentForbiddenActionException(
                    "Cannot pay for a cancelled booking (id=" + booking.getId() + ")");
        if (booking.getStatus() == BookingStatus.COMPLETED)
            throw new PaymentForbiddenActionException(
                    "Cannot pay for an already completed booking (id=" + booking.getId() + ")");
        if (booking.getStatus() == BookingStatus.CONFIRMED)
            throw new PaymentForbiddenActionException(
                    "This booking is already confirmed and paid (id=" + booking.getId() + ")");

        String paymentCode = generatePaymentCode();
        Payment payment = paymentMapper.toEntity(request, userId, paymentCode, booking);
        payment = paymentRepository.save(payment);

        payment = simulateMockGateway(payment);

        if (payment.getPaymentStatus() == PaymentStatus.SUCCEEDED) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Booking id={} auto-confirmed after successful payment", booking.getId());
        }

        log.info("Created payment id={} code={} status={} for bookingId={} userId={}",
                payment.getId(), paymentCode, payment.getPaymentStatus(), booking.getId(), userId);
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getById(Long paymentId) {
        return paymentMapper.toDto(findOrThrow(paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getByCode(String paymentCode) {
        return paymentMapper.toDto(
                paymentRepository.findByPaymentCode(paymentCode)
                        .orElseThrow(() -> new PaymentNotFoundException(null)));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponseDto> list(
            Long userId, Long bookingId, PaymentStatus status, PaymentMethod paymentMethod,
            Boolean fraudFlag, LocalDateTime dateFrom, LocalDateTime dateTo,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo))
            throw new IllegalArgumentException("dateFrom must not be after dateTo");

        Specification<Payment> spec = Specification
                .where(PaymentSpecifications.hasUserId(userId))
                .and(PaymentSpecifications.hasBookingId(bookingId))
                .and(PaymentSpecifications.hasStatus(status))
                .and(PaymentSpecifications.hasPaymentMethod(paymentMethod))
                .and(PaymentSpecifications.isFlagged(fraudFlag))
                .and(PaymentSpecifications.createdAfter(dateFrom))
                .and(PaymentSpecifications.createdBefore(dateTo))
                .and(PaymentSpecifications.minAmount(minAmount))
                .and(PaymentSpecifications.maxAmount(maxAmount));

        return toPagedResponse(paymentRepository.findAll(spec, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponseDto> listByHotelIds(
            List<Long> hotelIds, Long bookingId, PaymentStatus status, PaymentMethod paymentMethod,
            Boolean fraudFlag, LocalDateTime dateFrom, LocalDateTime dateTo,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {

        Specification<Payment> spec = Specification
                .where(PaymentSpecifications.hasHotelIdIn(hotelIds))
                .and(PaymentSpecifications.hasBookingId(bookingId))
                .and(PaymentSpecifications.hasStatus(status))
                .and(PaymentSpecifications.hasPaymentMethod(paymentMethod))
                .and(PaymentSpecifications.isFlagged(fraudFlag))
                .and(PaymentSpecifications.createdAfter(dateFrom))
                .and(PaymentSpecifications.createdBefore(dateTo))
                .and(PaymentSpecifications.minAmount(minAmount))
                .and(PaymentSpecifications.maxAmount(maxAmount));

        return toPagedResponse(paymentRepository.findAll(spec, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponseDto> getUserPaymentHistory(Long userId, Pageable pageable) {
        return toPagedResponse(paymentRepository.findByUserId(userId, pageable));
    }

    // flagForFraudDetection and getFlaggedPayments are handled entirely
    // by FraudCheckDecorator — no implementation needed here.
    @Override
    public void flagForFraudDetection(Long paymentId, Long managerId, String role) {
        throw new UnsupportedOperationException(
                "flagForFraudDetection must be called through FraudCheckDecorator");
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponseDto> getFlaggedPayments(PaymentStatus status, Pageable pageable) {
        Specification<Payment> spec = Specification
                .where(PaymentSpecifications.isFlagged(true))
                .and(PaymentSpecifications.hasStatus(status));
        return toPagedResponse(paymentRepository.findAll(spec, pageable));
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Payment simulateMockGateway(Payment payment) {
        payment.setPaymentIntentId(generatePaymentIntentId());
        payment.setPaymentStatus(PaymentStatus.PROCESSING);
        if (RANDOM.nextDouble() > 0.1) {
            payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
            payment.setProcessedAt(LocalDateTime.now());
            payment.setTransactionId(generateTransactionId());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailedAt(LocalDateTime.now());
            payment.setFailureReason("Card declined by issuer");
        }
        return paymentRepository.save(payment);
    }

    private Payment findOrThrow(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private PagedResponse<PaymentResponseDto> toPagedResponse(Page<Payment> page) {
        return new PagedResponse<>(
                page.map(paymentMapper::toDto).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private String generatePaymentCode() {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        return "PAY-" + year + "-" + String.format("%06d", RANDOM.nextInt(1000000));
    }

    private String generateTransactionId() {
        return "TXN-" + String.format("%08d", RANDOM.nextInt(100000000));
    }

    private String generatePaymentIntentId() {
        return "PI_" + String.format("%012d", RANDOM.nextInt(1000000000));
    }
}