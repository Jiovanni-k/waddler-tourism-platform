package org.example.rest.payment.refund;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.booking.Booking;
import org.example.rest.booking.BookingStatus;
import org.example.rest.payment.*;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {

    private static final Random RANDOM = new Random();

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final RefundMapper refundMapper;

    @Override
    @Transactional
    public RefundResponseDto createRefund(RefundRequestDto request) {
        Long requestedByUserId = SecurityUtil.getCurrentUserId();
        if (requestedByUserId == null)
            throw new IllegalArgumentException("Could not resolve authenticated user");

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException(request.getPaymentId()));

        String role = SecurityUtil.getCurrentUserRole();
        if ("USER".equals(role) && !payment.getUserId().equals(requestedByUserId)) {
            throw new AccessDeniedException("You can only request a refund for your own payments");
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCEEDED &&
                payment.getPaymentStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new RefundNotAllowedException(
                    "Payment cannot be refunded. Current status: " + payment.getPaymentStatus());
        }

        Booking booking = payment.getBooking();

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new RefundNotAllowedException(
                    "Refund is only allowed for cancelled bookings. Booking status: " + booking.getStatus());
        }

        BigDecimal refundAmount;
        BigDecimal refundPercentage;

        if (booking.getCancellationPolicyName() != null
                && booking.getCancellationDaysBeforeCheckin() != null
                && booking.getCancellationRefundPercentage() != null
                && booking.getCheckInDate() != null
                && booking.getTotalPrice() != null) {

            long daysUntilCheckIn = ChronoUnit.DAYS.between(
                    LocalDate.now(), booking.getCheckInDate());

            if (daysUntilCheckIn >= booking.getCancellationDaysBeforeCheckin()) {
                refundPercentage = booking.getCancellationRefundPercentage();
                if (refundPercentage.compareTo(BigDecimal.ZERO) == 0)
                    throw new RefundNotAllowedException(
                            "The cancellation policy '" +
                                    booking.getCancellationPolicyName().getDisplayName() +
                                    "' allows cancellation but with 0% refund. No refund is applicable.");
                refundAmount = BigDecimal.valueOf(booking.getTotalPrice())
                        .multiply(refundPercentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                log.info("Refund eligible — {}% of {} = {}", refundPercentage, booking.getTotalPrice(), refundAmount);
            } else {
                throw new RefundNotAllowedException(
                        "Cancellation deadline has passed. Policy: " +
                                booking.getCancellationPolicyName().getDisplayName() +
                                " requires cancellation at least " +
                                booking.getCancellationDaysBeforeCheckin() +
                                " days before check-in. Days remaining: " + daysUntilCheckIn);
            }
        } else {
            throw new RefundNotAllowedException(
                    "This booking has no cancellation policy. No refund is applicable.");
        }

        BigDecimal alreadyRefunded = refundRepository.findByPaymentId(payment.getId()).stream()
                .filter(r -> r.getStatus() == RefundStatus.COMPLETED)
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal availableForRefund = payment.getAmount().subtract(alreadyRefunded);
        if (refundAmount.compareTo(availableForRefund) > 0) {
            refundAmount = availableForRefund;
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new RefundNotAllowedException("No refundable amount remaining for this payment.");
        }

        String refundCode = generateRefundCode();
        Refund refund = refundMapper.toEntity(request, payment, refundCode, refundPercentage);
        refund.setAmount(refundAmount);
        refund.setStatus(RefundStatus.COMPLETED);
        refund.setProcessedAt(LocalDateTime.now());
        refund = refundRepository.save(refund);

        BigDecimal totalRefunded = alreadyRefunded.add(refundAmount);
        payment.setPaymentStatus(
                totalRefunded.compareTo(payment.getAmount()) >= 0
                        ? PaymentStatus.REFUNDED
                        : PaymentStatus.PARTIALLY_REFUNDED);
        paymentRepository.save(payment);

        log.info("Created refund id={} code={} amount={} for paymentId={} requestedBy={}",
                refund.getId(), refundCode, refundAmount, payment.getId(), requestedByUserId);
        return refundMapper.toDto(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponseDto getById(Long refundId) {
        return refundMapper.toDto(findOrThrow(refundId));
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponseDto getByCode(String refundCode) {
        return refundMapper.toDto(
                refundRepository.findByRefundCode(refundCode)
                        .orElseThrow(() -> new RefundNotFoundException(refundCode)));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RefundResponseDto> list(
            Long userId, Long paymentId, RefundStatus status,
            LocalDateTime dateFrom, LocalDateTime dateTo,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo))
            throw new IllegalArgumentException("dateFrom must not be after dateTo");

        Specification<Refund> spec = Specification
                .where(RefundSpecifications.hasUserId(userId))
                .and(RefundSpecifications.hasPaymentId(paymentId))
                .and(RefundSpecifications.hasStatus(status))
                .and(RefundSpecifications.createdAfter(dateFrom))
                .and(RefundSpecifications.createdBefore(dateTo))
                .and(RefundSpecifications.minAmount(minAmount))
                .and(RefundSpecifications.maxAmount(maxAmount));

        return toPagedResponse(refundRepository.findAll(spec, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RefundResponseDto> listByHotelIds(
            List<Long> hotelIds, Long paymentId, RefundStatus status,
            LocalDateTime dateFrom, LocalDateTime dateTo,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {

        Specification<Refund> spec = Specification
                .where(RefundSpecifications.hasHotelIdIn(hotelIds))
                .and(RefundSpecifications.hasPaymentId(paymentId))
                .and(RefundSpecifications.hasStatus(status))
                .and(RefundSpecifications.createdAfter(dateFrom))
                .and(RefundSpecifications.createdBefore(dateTo))
                .and(RefundSpecifications.minAmount(minAmount))
                .and(RefundSpecifications.maxAmount(maxAmount));

        return toPagedResponse(refundRepository.findAll(spec, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponseDto> getRefundsByPayment(Long paymentId, Long currentUserId, String role) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if ("USER".equals(role) && !payment.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only view refunds for your own payments");
        }

        return refundRepository.findByPaymentId(paymentId).stream()
                .map(refundMapper::toDto)
                .toList();
    }

    private Refund findOrThrow(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));
    }

    private PagedResponse<RefundResponseDto> toPagedResponse(Page<Refund> page) {
        return new PagedResponse<>(
                page.map(refundMapper::toDto).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private String generateRefundCode() {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        return "REF-" + year + "-" + String.format("%06d", RANDOM.nextInt(1000000));
    }
}