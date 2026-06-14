package org.example.rest.eventreservation;

public enum AgeRestriction {
    ALL_AGES("All Ages"),
    KIDS_ONLY("Kids Only"),
    TEENS("Teens"),
    ADULTS_ONLY("Adults Only"),
    SENIORS("Seniors");

    private final String displayName;

    AgeRestriction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}