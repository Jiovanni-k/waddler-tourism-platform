package org.example.rest.eventreservation;

public class EventReservationNotFoundException extends RuntimeException {
    public EventReservationNotFoundException(Long id) {
        super("Event reservation with id " + id + " not found");
    }
}