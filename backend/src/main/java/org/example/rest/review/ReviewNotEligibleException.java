package org.example.rest.review;

public class ReviewNotEligibleException extends RuntimeException {
    public ReviewNotEligibleException(String message) {
        super(message);
    }
}