package org.example.rest.payment;

public class PaymentForbiddenActionException extends RuntimeException {
    public PaymentForbiddenActionException(String message) {
        super(message);
    }
}