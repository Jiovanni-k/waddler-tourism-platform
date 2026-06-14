package org.example.rest.review;

public class ReviewNotAllowedException extends RuntimeException {
    public ReviewNotAllowedException(String message) {
        super(message);
    }
}