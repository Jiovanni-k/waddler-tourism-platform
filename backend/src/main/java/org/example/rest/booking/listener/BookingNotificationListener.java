package org.example.rest.booking.listener;

import lombok.RequiredArgsConstructor;
import org.example.rest.booking.Booking;
import org.example.rest.booking.event.BookingCancellationDeniedEvent;
import org.example.rest.booking.event.BookingCancelledEvent;
import org.example.rest.booking.event.BookingConfirmedEvent;
import org.example.rest.booking.event.BookingCreatedEvent;
import org.example.rest.loyalty.LoyaltyService;
import org.example.rest.notification.EmailService;
import org.example.rest.security.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Objects;
// Observer Pattern — This is the Observer.
// Observes all booking events published by BookingServiceImpl (the Subject)
// and handles all side-effects: emails and loyalty points.
// Adding a new notification means adding a new @EventListener here — zero changes to BookingServiceImpl.
@Component
@RequiredArgsConstructor
public class BookingNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(BookingNotificationListener.class);
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final LoyaltyService loyaltyService;

    // OBSERVE: BookingCreatedEvent — emails guest and manager
    @EventListener
    public void onBookingCreated(BookingCreatedEvent event) {
        Booking booking = event.getBooking();
        String hotelName = event.getHotelName();
        userRepository.findById(booking.getUserId()).ifPresent(user -> {
            try {
                emailService.sendBookingPendingToUser(user.getEmail(), user.getFirstName(), booking, hotelName);
            } catch (Exception e) {
                log.warn("Pending booking email to user could not be sent — {}", e.getMessage());
            }
        });
        Long managerId = event.getManagerId();
        if (managerId != null) {
            userRepository.findById(managerId).ifPresent(manager -> {
                try {
                    String guestName = userRepository.findById(booking.getUserId())
                            .map(u -> u.getFirstName() + " " + u.getLastName())
                            .orElse("Guest #" + booking.getUserId());
                    emailService.sendBookingPendingToManager(manager.getEmail(), manager.getFirstName(), booking, hotelName, guestName);
                } catch (Exception e) {
                    log.warn("Pending booking email to manager could not be sent — {}", e.getMessage());
                }});}}

    // OBSERVE: BookingConfirmedEvent — emails guest and awards loyalty points
    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        Booking booking = event.getBooking();
        String hotelName = event.getHotelName();
        userRepository.findById(booking.getUserId()).ifPresent(user -> {
            try {
                emailService.sendBookingConfirmation(user.getEmail(), user.getFirstName(), booking, hotelName);
            } catch (Exception e) {
                log.warn("Booking confirmation email could not be sent — {}", e.getMessage());
            }
        });
        try {
            loyaltyService.awardBookingPoints(booking.getUserId(), booking.getId(),
                    Objects.nonNull(booking.getNumberOfGuests()) ? booking.getNumberOfGuests() : 1);
        } catch (Exception e) {
            log.warn("Loyalty points could not be awarded — {}", e.getMessage());}}

    // OBSERVE: BookingCancelledEvent — emails cancellation + refund if refund > 0
    @EventListener
    public void onBookingCancelled(BookingCancelledEvent event) {
        Booking booking = event.getBooking();
        String hotelName = event.getHotelName();
        BigDecimal refund = event.getRefundAmount();
        userRepository.findById(booking.getUserId()).ifPresent(user -> {
            try {
                emailService.sendBookingCancellation(user.getEmail(), user.getFirstName(), booking, hotelName);
            } catch (Exception e) {
                log.warn("Booking cancellation email could not be sent — {}", e.getMessage());
            }});
        if (refund != null && refund.compareTo(BigDecimal.ZERO) > 0) {
            userRepository.findById(booking.getUserId()).ifPresent(user -> {
                try {
                    emailService.sendRefundConfirmation(user.getEmail(), user.getFirstName(), booking, hotelName);
                } catch (Exception e) {
                    log.warn("Refund confirmation email could not be sent — {}", e.getMessage());}
            });}}

    // OBSERVE: BookingCancellationDeniedEvent — emails policy-violation warning to guest
    @EventListener
    public void onBookingCancellationDenied(BookingCancellationDeniedEvent event) {
        Booking booking = event.getBooking();
        userRepository.findById(booking.getUserId()).ifPresent(user -> {
            try {
                emailService.sendCancellationPolicyViolation(user.getEmail(), user.getFirstName(),
                        booking, event.getHotelName(), event.getDaysUntilCheckIn());
            } catch (Exception e) {
                log.warn("Policy violation email could not be sent — {}", e.getMessage());
            }
        });
    }
}