package org.example.rest.payment.decorator;

import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.payment.*;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

// Decorator Pattern — Fraud Detection.
// Intercepts flagForFraudDetection() and enforces that a HOTEL_MANAGER
// can only flag payments belonging to their own hotels.
// All other methods pass straight through to the delegate.
@Slf4j
public class FraudCheckDecorator extends PaymentServiceDecorator {
    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;
    public FraudCheckDecorator(PaymentService delegate,
                               PaymentRepository paymentRepository, HotelRepository hotelRepository) {
        super(delegate);
        this.paymentRepository = paymentRepository;
        this.hotelRepository = hotelRepository;
    }
    @Override
    public void flagForFraudDetection(Long paymentId, Long managerId, String role) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if ("HOTEL_MANAGER".equals(role)) {
            Long hotelId = payment.getBooking().getRoom().getHotel().getId();
            boolean ownsHotel = hotelRepository.findByManagerId(managerId)
                    .stream().anyMatch(h -> h.getId().equals(hotelId));
            if (!ownsHotel) throw new AccessDeniedException(
                        "You can only flag payments for bookings in your own hotels");
        }
        payment.setFraudDetectionFlag(true);
        paymentRepository.save(payment);
        log.warn("Payment id={} flagged for fraud by managerId={}", paymentId, managerId);
    }

    @Override
    public PagedResponse<PaymentResponseDto> getFlaggedPayments(PaymentStatus status, Pageable pageable) {
        return delegate.getFlaggedPayments(status, pageable);
    }
}