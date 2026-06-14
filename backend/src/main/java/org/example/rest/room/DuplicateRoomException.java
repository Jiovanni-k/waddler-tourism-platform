package org.example.rest.room;

public class DuplicateRoomException extends RuntimeException {
    public DuplicateRoomException(String name, Long hotelId) {
        super("A room with name '" + name + "' already exists for hotel with id: " + hotelId);
    }
}