package org.example.rest.review;

import org.example.rest.ErrorMessages;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(Long id) {
        super(ErrorMessages.reviewNotFound(id));
    }
}