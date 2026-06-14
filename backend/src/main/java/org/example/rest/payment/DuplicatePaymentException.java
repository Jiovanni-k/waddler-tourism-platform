package org.example.rest.payment;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(Long bookingId) {
        super("A successful payment already exists for booking with id: " + bookingId);
    }
}