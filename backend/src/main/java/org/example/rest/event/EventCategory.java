package org.example.rest.event;

public enum EventCategory {

    CONCERT("Concert"),
    TRIP("Trip"),
    TOUR("Tour"),
    WORKSHOP("Workshop"),
    KIDS("Kids"),
    FOOD("Food & Drinks"),
    CULTURE("Culture"),
    PARTY("Party"),
    ADVENTURE("Adventure"),
    SPORTS("Sports"),
    WEDDING("Wedding"),
    CONFERENCE("Conference"),
    FAMILY("Family"),
    OTHER("Other");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}