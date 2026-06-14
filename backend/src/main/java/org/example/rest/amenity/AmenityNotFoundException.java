package org.example.rest.amenity;

import org.example.rest.ErrorMessages;

public class AmenityNotFoundException extends RuntimeException {
    public AmenityNotFoundException(Long id) {
        super(ErrorMessages.amenityNotFound(id));
    }
}