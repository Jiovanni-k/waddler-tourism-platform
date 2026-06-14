package org.example.rest.booking.event;

import org.example.rest.booking.Booking;

import java.math.BigDecimal;

/**
 * Observer Pattern — Event published when a booking is successfully cancelled.
 * BookingNotificationListener observes this and sends cancellation + refund emails.
 */
public class BookingCancelledEvent {

    private final Booking booking;
    private final String hotelName;
    private final BigDecimal refundAmount;

    public BookingCancelledEvent(Booking booking, String hotelName, BigDecimal refundAmount) {
        this.booking = booking;
        this.hotelName = hotelName;
        this.refundAmount = refundAmount;
    }

    public Booking getBooking() { return booking; }
    public String getHotelName() { return hotelName; }
    public BigDecimal getRefundAmount() { return refundAmount; }
}