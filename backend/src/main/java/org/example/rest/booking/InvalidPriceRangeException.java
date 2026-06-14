package org.example.rest.booking;


public class InvalidPriceRangeException extends RuntimeException {
    public InvalidPriceRangeException(String message) {
        super(message);
    }
}