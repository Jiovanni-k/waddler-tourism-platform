package org.example.rest.cancellationpolicy;

public class CancellationPolicyNotBelongToHotelException extends RuntimeException {
    public CancellationPolicyNotBelongToHotelException(Long policyId, Long hotelId) {
        super("Cancellation policy with id " + policyId + " does not belong to hotel with id " + hotelId);
    }
}