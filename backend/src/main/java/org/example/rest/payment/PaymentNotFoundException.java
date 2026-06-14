package org.example.rest.payment;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) {
        super(id == null ? "Payment not found" : "Payment not found with id: " + id);
    }
}