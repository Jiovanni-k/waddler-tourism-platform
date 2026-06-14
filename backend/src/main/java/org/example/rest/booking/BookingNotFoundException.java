package org.example.rest.booking;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long id) {
        super("Booking with id " + id + " not found");
    }
}