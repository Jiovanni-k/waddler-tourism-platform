package org.example.rest.event;

public enum EventStatus {

    DRAFT("Draft"),
    PUBLISHED("Published"),
    CANCELLED("Cancelled"),
    ENDED("Ended"),
    ARCHIVED("Archived");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}