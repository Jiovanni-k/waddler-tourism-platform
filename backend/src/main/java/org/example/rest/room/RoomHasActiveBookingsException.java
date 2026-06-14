package org.example.rest.room;

public class RoomHasActiveBookingsException extends RuntimeException {
    public RoomHasActiveBookingsException(Long roomId) {
        super("Room with id " + roomId + " is still active and has bookings linked to it. Deactivate the room first before deleting it.");
    }
}