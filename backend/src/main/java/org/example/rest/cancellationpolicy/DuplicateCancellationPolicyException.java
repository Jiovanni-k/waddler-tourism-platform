package org.example.rest.cancellationpolicy;

public class DuplicateCancellationPolicyException extends RuntimeException {
    public DuplicateCancellationPolicyException(String name, Long hotelId) {
        super("A cancellation policy with name '" + name + "' already exists for hotel with id " + hotelId);
    }
}