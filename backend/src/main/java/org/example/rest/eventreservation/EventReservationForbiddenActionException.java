package org.example.rest.eventreservation;

public class EventReservationForbiddenActionException extends RuntimeException {
    public EventReservationForbiddenActionException(String message) {
        super(message);
    }
}