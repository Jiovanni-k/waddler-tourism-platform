package org.example.rest.room;

import org.example.rest.ErrorMessages;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long id) {
        super(ErrorMessages.roomNotFound(id));
    }
}