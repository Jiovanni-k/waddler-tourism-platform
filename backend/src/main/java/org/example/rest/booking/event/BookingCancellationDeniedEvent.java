package org.example.rest.booking.event;

import org.example.rest.booking.Booking;

/**
 * Observer Pattern — Event published when a cancellation is DENIED due to policy violation.
 * BookingNotificationListener observes this and sends a policy-violation warning email.
 */
public class BookingCancellationDeniedEvent {

    private final Booking booking;
    private final String hotelName;
    private final long daysUntilCheckIn;

    public BookingCancellationDeniedEvent(Booking booking, String hotelName, long daysUntilCheckIn) {
        this.booking = booking;
        this.hotelName = hotelName;
        this.daysUntilCheckIn = daysUntilCheckIn;
    }

    public Booking getBooking() { return booking; }
    public String getHotelName() { return hotelName; }
    public long getDaysUntilCheckIn() { return daysUntilCheckIn; }
}