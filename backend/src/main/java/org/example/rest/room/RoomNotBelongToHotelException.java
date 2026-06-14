package org.example.rest.room;

public class RoomNotBelongToHotelException extends RuntimeException {
    public RoomNotBelongToHotelException(Long roomId, Long hotelId) {
        super("Room with id " + roomId + " does not belong to hotel with id " + hotelId);
    }
}