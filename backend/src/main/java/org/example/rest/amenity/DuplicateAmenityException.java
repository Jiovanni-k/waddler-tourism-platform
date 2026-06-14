package org.example.rest.amenity;

import org.example.rest.ErrorMessages;

public class DuplicateAmenityException extends RuntimeException {
    public DuplicateAmenityException() {
        super(ErrorMessages.DUPLICATE_AMENITY);
    }
}