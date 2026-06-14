package org.example.rest.review;

import org.example.rest.ErrorMessages;

public class AlreadyReviewedException extends RuntimeException {
    public AlreadyReviewedException() {
        super(ErrorMessages.ALREADY_REVIEWED);
    }
}