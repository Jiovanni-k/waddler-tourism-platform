package org.example.rest.review;

public enum ReviewSort {

    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    HIGHEST("Highest Rated"),
    LOWEST("Lowest Rated");

    private final String displayName;

    ReviewSort(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}