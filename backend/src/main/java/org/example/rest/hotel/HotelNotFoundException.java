package org.example.rest.hotel;

import org.example.rest.ErrorMessages;

public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException(Long id) {
        super(ErrorMessages.hotelNotFound(id));
    }
}