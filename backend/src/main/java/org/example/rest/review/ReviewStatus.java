package org.example.rest.review;

public enum ReviewStatus {

    PUBLISHED("Published"),
    HIDDEN("Hidden"),
    REPORTED("Reported");

    private final String displayName;

    ReviewStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}