package org.example.rest.review;

public enum ReviewTargetType {

    HOTEL("Hotel"),
    EVENT("Event");

    private final String displayName;

    ReviewTargetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}