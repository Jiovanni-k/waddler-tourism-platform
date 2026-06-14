package org.example.rest.cancellationpolicy;

public class CancellationPolicyNotFoundException extends RuntimeException {
    public CancellationPolicyNotFoundException(Long id) {
        super("Cancellation policy with id " + id + " not found");
    }
}