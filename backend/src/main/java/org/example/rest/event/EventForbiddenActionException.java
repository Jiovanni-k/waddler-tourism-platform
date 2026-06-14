package org.example.rest.event;

public class EventForbiddenActionException extends RuntimeException {
    public EventForbiddenActionException(String message) {
        super(message);
    }
}