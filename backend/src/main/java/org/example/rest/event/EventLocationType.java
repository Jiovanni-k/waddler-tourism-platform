package org.example.rest.event;

public enum EventLocationType {

    HOTEL("At the Hotel"),
    OUTSIDE("Outside Venue");

    private final String displayName;

    EventLocationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}