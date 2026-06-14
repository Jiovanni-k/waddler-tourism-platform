package org.example.rest.payment.refund;

public class RefundNotAllowedException extends RuntimeException {
    public RefundNotAllowedException(String message) {
        super(message);
    }
}