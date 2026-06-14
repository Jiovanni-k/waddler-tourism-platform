package org.example.rest.payment.decorator;

import org.example.rest.booking.Booking;
import org.example.rest.booking.BookingNotFoundException;
import org.example.rest.booking.BookingRepository;
import org.example.rest.payment.*;

import java.math.BigDecimal;

// Decorator Pattern — Amount & Split Validation.
// Intercepts createPayment() and validates:
//   1. Payment amount matches booking total exactly.
//   2. If method is SPLIT, splitBetweenUsers must be a valid JSON array.
// Then delegates to the next layer.
public class AmountValidationDecorator extends PaymentServiceDecorator {
    private final BookingRepository bookingRepository;
    public AmountValidationDecorator(PaymentService delegate, BookingRepository bookingRepository) {
        super(delegate);
        this.bookingRepository = bookingRepository;
    }
    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));
        if (booking.getTotalPrice() != null) {
            BigDecimal bookingTotal = BigDecimal.valueOf(booking.getTotalPrice());
            if (request.getAmount().compareTo(bookingTotal) != 0)
                throw new PaymentForbiddenActionException(
                        "Payment amount " + request.getAmount() + " does not match booking total " + bookingTotal);}

        if (request.getPaymentMethod() == PaymentMethod.SPLIT) {
            if (request.getSplitBetweenUsers() == null || request.getSplitBetweenUsers().isBlank())
                throw new IllegalArgumentException("Split payment info is required when method is SPLIT. " +
                                "Format: [{\"userId\": 1, \"amount\": 50.00}, {\"userId\": 2, \"amount\": 50.00}]");
            String json = request.getSplitBetweenUsers().trim();
            if (!json.startsWith("[") || !json.endsWith("]"))
                throw new IllegalArgumentException(
                        "Split payment must be a JSON array starting with [ and ending with ]");
        }
        return delegate.createPayment(request);
    }
}



