package org.example.rest.payment.refund;

public class RefundNotFoundException extends RuntimeException {
    public RefundNotFoundException(Long id) {
        super(id == null ? "Refund not found" : "Refund not found with id: " + id);
    }
    public RefundNotFoundException(String code) {
        super("Refund not found with code: " + code);
    }
}