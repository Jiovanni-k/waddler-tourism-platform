package org.example.rest.payment.decorator;

import org.example.rest.payment.*;

import java.util.List;

// Decorator Pattern — Duplicate Payment Guard.
// Intercepts createPayment() and throws if a SUCCEEDED or PROCESSING
// payment already exists for the same booking. Then delegates to next layer.
public class DuplicatePaymentGuardDecorator extends PaymentServiceDecorator {

    private final PaymentRepository paymentRepository;

    public DuplicatePaymentGuardDecorator(PaymentService delegate, PaymentRepository paymentRepository) {
        super(delegate);
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        boolean alreadyPaid = paymentRepository.existsByBooking_IdAndPaymentStatusIn(
                request.getBookingId(),
                List.of(PaymentStatus.SUCCEEDED, PaymentStatus.PROCESSING));
        if (alreadyPaid)
            throw new DuplicatePaymentException(request.getBookingId());

        return delegate.createPayment(request);
    }
}



