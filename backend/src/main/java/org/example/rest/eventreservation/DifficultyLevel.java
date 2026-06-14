package org.example.rest.eventreservation;

public enum DifficultyLevel {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    EXTREME("Extreme");

    private final String displayName;

    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}