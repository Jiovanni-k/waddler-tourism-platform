package org.example.rest.event;

import org.example.rest.ErrorMessages;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(Long id) {
        super(ErrorMessages.eventNotFound(id));
    }
}