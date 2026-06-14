package org.example.rest.tablereservation;

public enum TableReservationStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    NO_SHOW("No Show");

    private final String displayName;

    TableReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
