package org.example.rest.booking;

public class BookingForbiddenActionException extends RuntimeException {
    public BookingForbiddenActionException(String message) {
        super(message);
    }
}
