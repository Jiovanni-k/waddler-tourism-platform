package org.example.rest;

import lombok.*;

@Getter
@AllArgsConstructor
public class ApiError {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
}