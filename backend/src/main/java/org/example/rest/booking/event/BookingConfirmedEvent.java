package org.example.rest.booking.event;

import org.example.rest.booking.Booking;

/**
 * Observer Pattern — Event published when a booking is confirmed.
 * BookingNotificationListener observes this and sends a confirmation email + awards loyalty points.
 */
public class BookingConfirmedEvent {

    private final Booking booking;
    private final String hotelName;

    public BookingConfirmedEvent(Booking booking, String hotelName) {
        this.booking = booking;
        this.hotelName = hotelName;
    }

    public Booking getBooking() { return booking; }
    public String getHotelName() { return hotelName; }
}