package org.example.rest.tablereservation;

public enum SpecialOccasion {
    BIRTHDAY("Birthday"),
    ANNIVERSARY("Anniversary"),
    DATE("Date"),
    BUSINESS("Business"),
    FAMILY("Family"),
    OTHER("Other");

    private final String displayName;

    SpecialOccasion(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}