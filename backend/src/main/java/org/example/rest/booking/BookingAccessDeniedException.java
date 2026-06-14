package org.example.rest.booking;

public class BookingAccessDeniedException extends RuntimeException {
    public BookingAccessDeniedException(String message) {
        super(message);
    }
}