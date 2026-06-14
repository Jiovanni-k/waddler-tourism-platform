package org.example.rest.booking.event;

import org.example.rest.booking.Booking;

/**
 * Observer Pattern — Event published when a new booking is created (status = PENDING).
 * BookingNotificationListener observes this and sends emails to the guest and manager.
 */
public class BookingCreatedEvent {

    private final Booking booking;
    private final String hotelName;
    private final Long managerId;

    public BookingCreatedEvent(Booking booking, String hotelName, Long managerId) {
        this.booking = booking;
        this.hotelName = hotelName;
        this.managerId = managerId;
    }

    public Booking getBooking() { return booking; }
    public String getHotelName() { return hotelName; }
    public Long getManagerId() { return managerId; }
}