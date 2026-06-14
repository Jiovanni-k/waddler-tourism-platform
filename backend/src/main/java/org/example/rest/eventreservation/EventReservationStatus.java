package org.example.rest.eventreservation;

public enum EventReservationStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    ATTENDED("Attended"),
    NO_SHOW("No Show"),
    COMPLETED("Completed");

    private final String displayName;

    EventReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}