package org.example.rest.tablereservation;

public enum TableType {
    STANDARD("Standard"),
    PREMIUM("Premium"),
    OUTDOOR("Outdoor");

    private final String displayName;

    TableType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
